//#include "stdafx.h"
#include "comm.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[]=__FILE__;
#endif
IMPLEMENT_DYNCREATE(CComm, CObject)

CComm::CComm()
{
	hComm=NULL;  //comport 핸들 초기화
	bFlowCtrl=FC_XONXOFF;  //flow_control 설정
	fConnected=FALSE;  //thread stop
//	m_pToServoMsg = _T(""); //CString 초기화
//	Rev_Sel=0;
//	m_pServoEnableflag = FALSE;
}

CComm::~CComm()
{
	DestroyComm();
}

//통신을 하는 프로시저, 감시하는 루틴, 본 루팅는 OpenComPort함수 실행시 프로시저로 연결됨
DWORD CommWatchProc(LPVOID lpData)
{
	DWORD dwEvtMask;
	OVERLAPPED os;   //overlap 구조체 선언
	CComm *npComm = (CComm *)lpData;  //CComm 클래스 포인터 선언
	char InData[MAXBLOCK+1];  //receive 데이터 저장하는 배열
	int nLength;  //receive 데이터 길이를 저장하는 변수
	if(npComm==NULL) return -1; //npComm 라는 핸들에 아무런 컴포트가 안 붙어 있으면 에러리턴

	memset(&os, 0, sizeof(OVERLAPPED));  //overlap 구조체 os 를 초기화
    os.hEvent=CreateEvent(NULL, //no security
		TRUE,  //explicit reset req
		FALSE, //initial event reset
		NULL); //no name

	//event 생성 실패
	if(os.hEvent==NULL) {
		AfxMessageBox("Fail to Create Event!", MB_OK, 0);

        return FALSE;
	}

	//EV_RXCHAR 을 이벤트로 설정, 다른 이벤트는 무시함
	if(!SetCommMask(npComm->hComm, EV_RXCHAR)) return FALSE;

	//fConnected가 TRUE 일때만 EVENT 를 기다림
	while(npComm->fConnected) {
		dwEvtMask=0; //생성된 EVENT를 저장하는 변수 
		WaitCommEvent(npComm->hComm, &dwEvtMask, NULL);	//EVENT가 발생하기를 기다림
		if((dwEvtMask & EV_RXCHAR) == EV_RXCHAR) {	//EV_RXCHAR 이벤트가 발생하면
			do {
				memset(InData, 0, 1024); //InData 배열 0으로 초기화
				//ReadCommBlock함수에서 버퍼에 데이터가 있는지를 확인한다.
				if(nLength = npComm->ReadCommBlock((LPSTR)InData, MAXBLOCK)) {
					npComm->SetReadData(InData);
					//이곳에서 데이터를 받는다.
				}
			}
			while(nLength>0); //데이터를 읽으면 버퍼에서 읽은 데이터는 사라지므로
			                  //버퍼에 있는 데이터를 다 읽음.
		}
	}

	CloseHandle(os.hEvent);

	return TRUE;
}

//receive 데이타를 data 에 복사한다.
void CComm::SetReadData(LPSTR data)
{
	if(m_bStartFlag == TRUE)
	{
		m_pInBuffer += (LPSTR)data;

		// 설정된 윈도에 WM_RECEIVEDATA 메시지를 날려주어 현재 데이터가 들어왔다는 것을 알려준다.
		SendMessage(m_hWnd, WM_RECEIVEDATA, (WPARAM)hComm, 0); 
	}
}

//메시지를 전달할 hWnd 설정
void CComm::SetHwnd(HWND hwnd)
{
	m_hWnd=hwnd;
}

//컴포트를 설정한다.
void CComm::SetComport(int port, DWORD rate, BYTE byteSize, BYTE stop, BYTE parity)
{
	bPort=port;
	dwBaudRate=rate;
	bByteSize=byteSize;
	bStopBits=stop;
	bParity=parity;
}

//XonOff, 리턴값 더블 설정
void CComm::SetXonOff(BOOL chk)
{
	fXonXoff=chk;
}

void CComm::SetDtrRts(BYTE chk)
{
	bFlowCtrl=chk;
}

//컴포트 정보 생성
//SetComport()->SetXonOff()->SetDtrRts()한다음 설정한다.
BOOL CComm::CreateCommInfo()
{
	osWrite.Offset=0;
	osWrite.OffsetHigh=0;
	osRead.Offset=0;
	osRead.OffsetHigh=0;

	//이벤트를 생성한다. 수동리셋이벤트, 초기비신호상태
	osRead.hEvent=CreateEvent(NULL, TRUE, FALSE, NULL);
	if(osRead.hEvent=NULL) {

		return FALSE;
	}
	osWrite.hEvent=CreateEvent(NULL, TRUE, FALSE, NULL);
	if(osWrite.hEvent=NULL) {
		CloseHandle(osRead.hEvent);

		return FALSE;
	}

	return TRUE;
}

//컴포트를 열고 연결을 시도한다.
BOOL CComm::OpenComport()
{
	char szPort[15];
	BOOL fRetVal;
	COMMTIMEOUTS CommTimeOuts;
	if(bPort>10)
		lstrcpy(szPort, "error");
	else 
		wsprintf(szPort, "COM%d", bPort);

	if((hComm=CreateFile(szPort, GENERIC_READ | GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OVERLAPPED,
		NULL)) == (HANDLE)-1)
		return FALSE;
	else {
		//컴포트에서 데이터를 교환하는 방법을 char단위를 기본으로 설정
		SetCommMask(hComm, EV_RXCHAR);
		SetupComm(hComm, 4096, 4096);
		//디바이스에 쓰레기가 있을지 모르니까 깨끗이 청소를 한다.
		PurgeComm(hComm, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
		CommTimeOuts.ReadIntervalTimeout=0xFFFFFFFF;
		CommTimeOuts.ReadTotalTimeoutMultiplier=0;
		CommTimeOuts.ReadTotalTimeoutConstant=1000;
		CommTimeOuts.WriteTotalTimeoutMultiplier=0;
		CommTimeOuts.WriteTotalTimeoutConstant=1000;
		SetCommTimeouts(hComm, &CommTimeOuts);
	}

	fRetVal = SetupConnection();
	if(fRetVal) {	//연결이 되었다면 fRetVal 이 TRUE 이므로
		fConnected=TRUE;  //연결되었다고 말해줌
		AfxBeginThread((AFX_THREADPROC)CommWatchProc,(LPVOID)this, THREAD_PRIORITY_NORMAL, 0, 0, NULL);
	}
	else {
		fConnected=FALSE;
		AfxMessageBox("통신 포트에 문제가 발생했습니다.", MB_OK);
		CloseHandle(hComm);
	}

	return fRetVal;
}

//파일로 설정된 컴포트와 실질 포트를 연결시킨다.
//SetupConnection 이전에 CreateComport를 해주어야 한다.
BOOL CComm::SetupConnection()
{
	BOOL fRetVal;
	//BYTE bSet;
	DCB dcb;
	dcb.DCBlength=sizeof(DCB);
	GetCommState(hComm, &dcb);  //dcb의 기본값을 받는다.

	dcb.BaudRate=dwBaudRate;
	dcb.ByteSize=bByteSize;
	dcb.Parity=bParity;
	dcb.StopBits=bStopBits;
	
	fRetVal=SetCommState(hComm, &dcb);

	return fRetVal;
}

//컴포트로부터 데이터를 읽는다.
int CComm::ReadCommBlock(LPSTR lpszBlock, int nMaxLength)
{
	BOOL fReadStat;
	COMSTAT ComStat;
	DWORD dwErrorFlags;
	DWORD dwLength;

	//only try to read number of bytes in queue
	ClearCommError(hComm, &dwErrorFlags, &ComStat);
	dwLength=min( (DWORD)nMaxLength, ComStat.cbInQue);
	if (dwLength>0) {
		fReadStat=ReadFile(hComm, lpszBlock, dwLength, &dwLength, &osRead);
		if (!fReadStat) {
			//Error Message
		}
	}
	return dwLength;
}

//컴포트를 완전히 해제한다.
BOOL CComm::DestroyComm()
{
	if(fConnected) CloseConnection();
	CloseHandle(osRead.hEvent);
	CloseHandle(osWrite.hEvent);

	return TRUE;
}

//연결을 닫는다.
BOOL CComm::CloseConnection()
{
	//set connected flag to FALSE;
	fConnected=FALSE;
	//disable event notification and wait for thread to halt
	SetCommMask(hComm, 0);
	EscapeCommFunction(hComm, CLRDTR);
	PurgeComm(hComm, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
	CloseHandle(hComm);

	return TRUE;
}

BOOL CComm::WriteCommBlock(LPSTR lpByte, DWORD dwBytesToWrite)
{
	BOOL fWriteStat;
	DWORD dwBytesWritten;
	fWriteStat=WriteFile(hComm, lpByte, dwBytesToWrite, &dwBytesWritten, &osWrite);
	if(!fWriteStat) {
		//Error Message
	}

	return TRUE;
}

