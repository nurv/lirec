// iCatChess.cpp : main project file.
#include "stdafx.h"
#include "Form1.h"

using namespace System::Threading;
using namespace System::Windows::Forms;
using namespace iCatChess;


void ThreadProc(Object ^ frm)
{
		// Enabling Windows XP visual effects before any controls are created
	Application::EnableVisualStyles();
	Application::SetCompatibleTextRenderingDefault(false); 

	// Create the main window and run it

	
	Application::Run((Form1 ^)frm);
    
};

[STAThreadAttribute]
int main(array<System::String ^> ^args)
{
	System::Windows::Forms::Control::CheckForIllegalCrossThreadCalls=false;
	
	
	//System::Windows::Forms::ApplicationContext apc;
	Form1 ^ frm = gcnew Form1();
	ChessModule *pChessModule = new ChessModule (frm, true);
	
	Thread^ t = gcnew Thread(gcnew ParameterizedThreadStart(ThreadProc));
		//gcnew ThreadStart(ThreadProc));
	t->Start(frm);
	
	//frm->label1->Text="andre";
	//FormCollection fc = gcnew FormCollection();
	//Application::OpenForms::get()->GetEnumerator()->MoveNext()
	
		
	//andre->label1->Name= L"sada";
	//Form1::label1->Name=L"sada";

try
   {
	   printf("iolanda iolanda");
      // Create the module
     // ChessModule *pChessModule = new ChessModule ();
	
      if (pChessModule != NULL)
      {
         // Start the run cycle
         pChessModule->vRun(Loop_e, NO_TIMEOUT);

         // Cleanup the module
         delete pChessModule;
      } // if
   }
   catch (const DMLException &e)
   {
      // Print the reason of any thrown exception
      printf ("========================== EXCEPTION RAISED ========================\n");
      printf ("%s\n", e.pszGiveReason());
      printf ("====================================================================\n");
   }

	return 0;
}

