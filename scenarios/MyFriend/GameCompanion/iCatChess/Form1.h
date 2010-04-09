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
	public: System::Windows::Forms::Button^  button1;
	protected: 
	public: System::Windows::Forms::Label^  label1;
	public: System::Windows::Forms::TextBox^  textBox1;
	public: System::Windows::Forms::Label^  label2;
	public: System::Windows::Forms::TextBox^  textBox2;
	public: System::Windows::Forms::RichTextBox^  richTextBox1;


	public: System::Windows::Forms::Button^  button3;
	public: System::Windows::Forms::Button^  button4;
	public: System::Windows::Forms::Button^  button5;
	public: System::Windows::Forms::Button^  newGameBut;
	public: System::Windows::Forms::TextBox^  textBox3;
	public: System::Windows::Forms::Label^  label3;
	public: System::Windows::Forms::TextBox^  textBox4;
	public: System::Windows::Forms::Label^  label4;
	public: System::Windows::Forms::TextBox^  textBox5;
	public: System::Windows::Forms::Label^  label5;
	public: System::Windows::Forms::Button^  buttdesisto;










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
			this->button1 = (gcnew System::Windows::Forms::Button());
			this->label1 = (gcnew System::Windows::Forms::Label());
			this->textBox1 = (gcnew System::Windows::Forms::TextBox());
			this->label2 = (gcnew System::Windows::Forms::Label());
			this->textBox2 = (gcnew System::Windows::Forms::TextBox());
			this->richTextBox1 = (gcnew System::Windows::Forms::RichTextBox());
			this->button3 = (gcnew System::Windows::Forms::Button());
			this->button4 = (gcnew System::Windows::Forms::Button());
			this->button5 = (gcnew System::Windows::Forms::Button());
			this->newGameBut = (gcnew System::Windows::Forms::Button());
			this->textBox3 = (gcnew System::Windows::Forms::TextBox());
			this->label3 = (gcnew System::Windows::Forms::Label());
			this->textBox4 = (gcnew System::Windows::Forms::TextBox());
			this->label4 = (gcnew System::Windows::Forms::Label());
			this->textBox5 = (gcnew System::Windows::Forms::TextBox());
			this->label5 = (gcnew System::Windows::Forms::Label());
			this->buttdesisto = (gcnew System::Windows::Forms::Button());
			this->SuspendLayout();
			// 
			// button1
			// 
			this->button1->Location = System::Drawing::Point(51, 112);
			this->button1->Name = L"button1";
			this->button1->Size = System::Drawing::Size(75, 23);
			this->button1->TabIndex = 0;
			this->button1->Text = L"Jogar";
			this->button1->UseVisualStyleBackColor = true;
			this->button1->Click += gcnew System::EventHandler(this, &Form1::button1_Click);
			// 
			// label1
			// 
			this->label1->AutoSize = true;
			this->label1->Location = System::Drawing::Point(25, 28);
			this->label1->Name = L"label1";
			this->label1->Size = System::Drawing::Size(136, 13);
			this->label1->TabIndex = 1;
			this->label1->Text = L"A Minha Ultima Jogada Foi:";
			// 
			// textBox1
			// 
			this->textBox1->Location = System::Drawing::Point(42, 44);
			this->textBox1->Name = L"textBox1";
			this->textBox1->ReadOnly = true;
			this->textBox1->Size = System::Drawing::Size(100, 20);
			this->textBox1->TabIndex = 2;
			// 
			// label2
			// 
			this->label2->AutoSize = true;
			this->label2->Location = System::Drawing::Point(44, 70);
			this->label2->Name = L"label2";
			this->label2->Size = System::Drawing::Size(97, 13);
			this->label2->TabIndex = 3;
			this->label2->Text = L"Faz A Tua Jogada:";
			// 
			// textBox2
			// 
			this->textBox2->Location = System::Drawing::Point(42, 86);
			this->textBox2->Name = L"textBox2";
			this->textBox2->Size = System::Drawing::Size(100, 20);
			this->textBox2->TabIndex = 4;
			this->textBox2->TextChanged += gcnew System::EventHandler(this, &Form1::textBox2_TextChanged);
			// 
			// richTextBox1
			// 
			this->richTextBox1->Location = System::Drawing::Point(28, 141);
			this->richTextBox1->Name = L"richTextBox1";
			this->richTextBox1->Size = System::Drawing::Size(547, 319);
			this->richTextBox1->TabIndex = 5;
			this->richTextBox1->Text = L"";
			// 
			// button3
			// 
			this->button3->Location = System::Drawing::Point(176, 110);
			this->button3->Name = L"button3";
			this->button3->Size = System::Drawing::Size(75, 23);
			this->button3->TabIndex = 7;
			this->button3->Text = L"Empate";
			this->button3->UseVisualStyleBackColor = true;
			this->button3->Click += gcnew System::EventHandler(this, &Form1::button3_Click);
			// 
			// button4
			// 
			this->button4->Location = System::Drawing::Point(194, 18);
			this->button4->Name = L"button4";
			this->button4->Size = System::Drawing::Size(107, 40);
			this->button4->TabIndex = 8;
			this->button4->Text = L"iCat propor empate";
			this->button4->UseVisualStyleBackColor = true;
			this->button4->Click += gcnew System::EventHandler(this, &Form1::button4_Click);
			// 
			// button5
			// 
			this->button5->Location = System::Drawing::Point(194, 63);
			this->button5->Name = L"button5";
			this->button5->Size = System::Drawing::Size(106, 37);
			this->button5->TabIndex = 9;
			this->button5->Text = L"ok, empate";
			this->button5->UseVisualStyleBackColor = true;
			this->button5->Click += gcnew System::EventHandler(this, &Form1::button5_Click);
			// 
			// newGameBut
			// 
			this->newGameBut->Location = System::Drawing::Point(360, 13);
			this->newGameBut->Name = L"newGameBut";
			this->newGameBut->Size = System::Drawing::Size(107, 117);
			this->newGameBut->TabIndex = 10;
			this->newGameBut->Text = L"New Game";
			this->newGameBut->UseVisualStyleBackColor = true;
			this->newGameBut->Click += gcnew System::EventHandler(this, &Form1::newGameBut_Click);
			// 
			// textBox3
			// 
			this->textBox3->Location = System::Drawing::Point(474, 28);
			this->textBox3->Name = L"textBox3";
			this->textBox3->Size = System::Drawing::Size(100, 20);
			this->textBox3->TabIndex = 12;
			this->textBox3->Text = L"0";
			// 
			// label3
			// 
			this->label3->AutoSize = true;
			this->label3->Location = System::Drawing::Point(476, 12);
			this->label3->Name = L"label3";
			this->label3->Size = System::Drawing::Size(77, 13);
			this->label3->TabIndex = 11;
			this->label3->Text = L"Side To Move:";
			// 
			// textBox4
			// 
			this->textBox4->Location = System::Drawing::Point(475, 70);
			this->textBox4->Name = L"textBox4";
			this->textBox4->Size = System::Drawing::Size(100, 20);
			this->textBox4->TabIndex = 14;
			this->textBox4->Text = L"1";
			// 
			// label4
			// 
			this->label4->AutoSize = true;
			this->label4->Location = System::Drawing::Point(477, 54);
			this->label4->Name = L"label4";
			this->label4->Size = System::Drawing::Size(52, 13);
			this->label4->TabIndex = 13;
			this->label4->Text = L"iCat Side:";
			// 
			// textBox5
			// 
			this->textBox5->Location = System::Drawing::Point(474, 112);
			this->textBox5->Name = L"textBox5";
			this->textBox5->Size = System::Drawing::Size(100, 20);
			this->textBox5->TabIndex = 16;
			this->textBox5->Text = L"15";
			// 
			// label5
			// 
			this->label5->AutoSize = true;
			this->label5->Location = System::Drawing::Point(476, 96);
			this->label5->Name = L"label5";
			this->label5->Size = System::Drawing::Size(39, 13);
			this->label5->TabIndex = 15;
			this->label5->Text = L"Castle:";
			// 
			// buttdesisto
			// 
			this->buttdesisto->Location = System::Drawing::Point(265, 110);
			this->buttdesisto->Name = L"buttdesisto";
			this->buttdesisto->Size = System::Drawing::Size(75, 23);
			this->buttdesisto->TabIndex = 17;
			this->buttdesisto->Text = L"Desisto";
			this->buttdesisto->UseVisualStyleBackColor = true;
			this->buttdesisto->Click += gcnew System::EventHandler(this, &Form1::buttdesisto_Click);
			// 
			// Form1
			// 
			this->AutoScaleDimensions = System::Drawing::SizeF(6, 13);
			this->AutoScaleMode = System::Windows::Forms::AutoScaleMode::Font;
			this->ClientSize = System::Drawing::Size(604, 494);
			this->Controls->Add(this->buttdesisto);
			this->Controls->Add(this->textBox5);
			this->Controls->Add(this->label5);
			this->Controls->Add(this->textBox4);
			this->Controls->Add(this->label4);
			this->Controls->Add(this->textBox3);
			this->Controls->Add(this->label3);
			this->Controls->Add(this->newGameBut);
			this->Controls->Add(this->button5);
			this->Controls->Add(this->button4);
			this->Controls->Add(this->button3);
			this->Controls->Add(this->richTextBox1);
			this->Controls->Add(this->textBox2);
			this->Controls->Add(this->label2);
			this->Controls->Add(this->textBox1);
			this->Controls->Add(this->label1);
			this->Controls->Add(this->button1);
			this->Name = L"Form1";
			this->Text = L"Form1";
			this->Load += gcnew System::EventHandler(this, &Form1::Form1_Load);
			this->ResumeLayout(false);
			this->PerformLayout();

		}
#pragma endregion
	private: System::Void button1_Click(System::Object^  sender, System::EventArgs^  e) {
					 button1->Enabled=false;
					 textBox2->Enabled=false;
			 }
private: System::Void textBox2_TextChanged(System::Object^  sender, System::EventArgs^  e) {
		 }
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
};
}

