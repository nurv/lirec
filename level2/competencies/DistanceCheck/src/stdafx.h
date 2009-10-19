
#pragma once

#ifndef _WIN32_WINNT		                  
#define _WIN32_WINNT 0x0501	
#endif						

#include <stdio.h>

// added by dave griffiths - no tchar.h on linux, 
// not sure what the standard windows #define is though
#ifdef _WIN32
#include <tchar.h>
#endif						
