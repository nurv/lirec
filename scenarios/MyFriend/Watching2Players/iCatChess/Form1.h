#pragma once


namespace iCatChess {

	using namespace System;
	using namespace System::ComponentModel;
	using namespace System::Collections;
	using namespace System::Windows::Forms;
	using namespace System::Data;
	using namespace System::Drawing;

	/// <summary>
	/// Summary for Form1
	///
	/// WARNING: If you change the name of this class, you will need to change the
	///          'Resource File Name' property for the managed resource compiler tool
	///          associated with all .resx files this class depends on.  Otherwise,
	///          the designers will not be able to interact properly with localized
	///          resources associated with this form.
	/// </summary>
	public ref class Form1 : public System::Windows::Forms::Form
	{
	public:
		Form1(void)
		{
			InitializeComponent();
			//
			//TODO: Add the constructor code here
			//
		}

	protected:
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		~Form1()
		{
			if (components)
			{
				delete components;
			}
		}

	protected: 
	public: System::Windows::Forms::RichTextBox^  richTextBox1;
	public: System::Windows::Forms::Button^  button3;
	public: System::Windows::Forms::Button^  button4;
	public: System::Windows::Forms::Button^  button5;
	public: System::Windows::Forms::Button^  newGameBut;




	public: System::Windows::Forms::TextBox^  textBox5;
	public: System::Windows::Forms::Label^  label5;
	public: System::Windows::Forms::Button^  buttdesisto;
	public: System::Windows::Forms::TextBox^  tboxName;

	public: System::Windows::Forms::Label^  label6;
	public: System::Windows::Forms::TextBox^  tBoxOppName;
	public: System::Windows::Forms::Button^  buttonRestart;

	public: 

	public: 

	public: 

	public: 

	public: 

	public: 

	public: 

	public: 


	public: 

	public: 

	public: 

	public: 

	public: 

	public: 

	protected: 



	private:
		/// <summary>
		/// Required designer variable.
		/// </summary>
		System::ComponentModel::Container ^components;

#pragma region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		void InitializeComponent(void)
		{
			this->richTextBox1 = (gcnew System::Windows::Forms::RichTextBox());
			this->button3 = (gcnew System::Windows::Forms::Button());
			this->button4 = (gcnew System::Windows::Forms::Button());
			this->button5 = (gcnew System::Windows::Forms::Button());
			this->newGameBut = (gcnew System::Windows::Forms::Button());
			this->textBox5 = (gcnew System::Windows::Forms::TextBox());
			this->label5 = (gcnew System::Windows::Forms::Label());
			this->buttdesisto = (gcnew System::Windows::Forms::Button());
			this->tboxName = (gcnew System::Windows::Forms::TextBox());
			this->label6 = (gcnew System::Windows::Forms::Label());
			this->tBoxOppName = (gcnew System::Windows::Forms::TextBox());
			this->buttonRestart = (gcnew System::Windows::Forms::Button());
			this->SuspendLayout();
			// 
			// richTextBox1
			// 
			this->richTextBox1->Location = System::Drawing::Point(12, 67);
			this->richTextBox1->Name = L"richTextBox1";
			this->richTextBox1->Size = System::Drawing::Size(676, 393);
			this->richTextBox1->TabIndex = 5;
			this->richTextBox1->Text = L"";
			// 
			// button3
			// 
			this->button3->Location = System::Drawing::Point(28, 36);
			this->button3->Name = L"button3";
			this->button3->Size = System::Drawing::Size(107, 25);
			this->button3->TabIndex = 7;
			this->button3->Text = L"User Gave Up";
			this->button3->UseVisualStyleBackColor = true;
			this->button3->Click += gcnew System::EventHandler(this, &Form1::button3_Click);
			// 
			// button4
			// 
			this->button4->Location = System::Drawing::Point(28, 5);
			this->button4->Name = L"button4";
			this->button4->Size = System::Drawing::Size(107, 25);
			this->button4->TabIndex = 8;
			this->button4->Text = L"Propose a Draw";
			this->button4->UseVisualStyleBackColor = true;
			this->button4->Click += gcnew System::EventHandler(this, &Form1::button4_Click);
			// 
			// button5
			// 
			this->button5->Location = System::Drawing::Point(141, 5);
			this->button5->Name = L"button5";
			this->button5->Size = System::Drawing::Size(107, 25);
			this->button5->TabIndex = 9;
			this->button5->Text = L"Draw";
			this->button5->UseVisualStyleBackColor = true;
			this->button5->Click += gcnew System::EventHandler(this, &Form1::button5_Click);
			// 
			// newGameBut
			// 
			this->newGameBut->Enabled = false;
			this->newGameBut->Location = System::Drawing::Point(439, 7);
			this->newGameBut->Name = L"newGameBut";
			this->newGameBut->Size = System::Drawing::Size(124, 28);
			this->newGameBut->TabIndex = 10;
			this->newGameBut->Text = L"New Game";
			this->newGameBut->UseVisualStyleBackColor = true;
			this->newGameBut->Click += gcnew System::EventHandler(this, &Form1::newGameBut_Click);
			// 
			// textBox5
			// 
			this->textBox5->Location = System::Drawing::Point(569, 30);
			this->textBox5->Name = L"textBox5";
			this->textBox5->Size = System::Drawing::Size(100, 20);
			this->textBox5->TabIndex = 16;
			this->textBox5->Text = L"15";
			// 
			// label5
			// 
			this->label5->AutoSize = true;
			this->label5->Location = System::Drawing::Point(569, 14);
			this->label5->Name = L"label5";
			this->label5->Size = System::Drawing::Size(39, 13);
			this->label5->TabIndex = 15;
			this->label5->Text = L"Castle:";
			// 
			// buttdesisto
			// 
			this->buttdesisto->Location = System::Drawing::Point(141, 36);
			this->buttdesisto->Name = L"buttdesisto";
			this->buttdesisto->Size = System::Drawing::Size(107, 25);
			this->buttdesisto->TabIndex = 17;
			this->buttdesisto->Text = L"iCat gives Up";
			this->buttdesisto->UseVisualStyleBackColor = true;
			this->buttdesisto->Click += gcnew System::EventHandler(this, &Form1::buttdesisto_Click);
			// 
			// tboxName
			// 
			this->tboxName->Location = System::Drawing::Point(254, 15);
			this->tboxName->Name = L"tboxName";
			this->tboxName->Size = System::Drawing::Size(179, 20);
			this->tboxName->TabIndex = 20;
			this->tboxName->TextChanged += gcnew System::EventHandler(this, &Form1::textBox6_TextChanged);
			// 
			// label6
			// 
			this->label6->AutoSize = true;
			this->label6->Location = System::Drawing::Point(254, -1);
			this->label6->Name = L"label6";
			this->label6->Size = System::Drawing::Size(134, 13);
			this->label6->TabIndex = 19;
			this->label6->Text = L"1- Companion 2- Opponent";
			this->label6->Click += gcnew System::EventHandler(this, &Form1::label6_Click);
			// 
			// tBoxOppName
			// 
			this->tBoxOppName->Location = System::Drawing::Point(254, 41);
			this->tBoxOppName->Name = L"tBoxOppName";
			this->tBoxOppName->Size = System::Drawing::Size(179, 20);
			this->tBoxOppName->TabIndex = 21;
			// 
			// buttonRestart
			// 
			this->buttonRestart->Location = System::Drawing::Point(439, 36);
			this->buttonRestart->Name = L"buttonRestart";
			this->buttonRestart->Size = System::Drawing::Size(124, 25);
			this->buttonRestart->TabIndex = 22;
			this->buttonRestart->Text = L"Restart";
			this->buttonRestart->UseVisualStyleBackColor = true;
			this->buttonRestart->Click += gcnew System::EventHandler(this, &Form1::buttonRestart_Click);
			// 
			// Form1
			// 
			this->AutoScaleDimensions = System::Drawing::SizeF(6, 13);
			this->AutoScaleMode = System::Windows::Forms::AutoScaleMode::Font;
			this->ClientSize = System::Drawing::Size(700, 469);
			this->Controls->Add(this->buttonRestart);
			this->Controls->Add(this->tBoxOppName);
			this->Controls->Add(this->tboxName);
			this->Controls->Add(this->label6);
			this->Controls->Add(this->buttdesisto);
			this->Controls->Add(this->textBox5);
			this->Controls->Add(this->label5);
			this->Controls->Add(this->newGameBut);
			this->Controls->Add(this->button5);
			this->Controls->Add(this->button4);
			this->Controls->Add(this->button3);
			this->Controls->Add(this->richTextBox1);
			this->Name = L"Form1";
			this->Text = L"Form1";
			this->Load += gcnew System::EventHandler(this, &Form1::Form1_Load);
			this->ResumeLayout(false);
			this->PerformLayout();

		}
#pragma endregion


private: System::Void Form1_Load(System::Object^  sender, System::EventArgs^  e) {
			// this->reportViewer1->RefreshReport();
		 }

private: System::Void button2_Click(System::Object^  sender, System::EventArgs^  e) {
			   buttdesisto->Enabled=false;
		 }
private: System::Void button3_Click(System::Object^  sender, System::EventArgs^  e) {
			 button3->Enabled=false;
		 }
private: System::Void button4_Click(System::Object^  sender, System::EventArgs^  e) {
			 button4->Enabled = false;
		 }
private: System::Void button5_Click(System::Object^  sender, System::EventArgs^  e) {
			 button5->Enabled = false;
		 }
private: System::Void newGameBut_Click(System::Object^  sender, System::EventArgs^  e) {
			 newGameBut->Enabled = false;
		 }
private: System::Void buttdesisto_Click(System::Object^  sender, System::EventArgs^  e) {
			 buttdesisto->Enabled=false;
		 }
private: System::Void label6_Click(System::Object^  sender, System::EventArgs^  e) {
		 }
private: System::Void textBox6_TextChanged(System::Object^  sender, System::EventArgs^  e) {
		 }
private: System::Void button2_Click_1(System::Object^  sender, System::EventArgs^  e) {
		 }
private: System::Void buttonRestart_Click(System::Object^  sender, System::EventArgs^  e) {
			buttdesisto->Enabled=false;
		 }
};
}

