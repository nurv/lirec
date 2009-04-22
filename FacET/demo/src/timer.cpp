/* $Id: timer.cpp 5 2009-03-12 22:30:56Z mw $
 * +-------------------------------------------------------------------+
 * | This file contains parts of code from the application created for |
 * | the Master thesis supervised by Marek Wnuk (Wroclaw University of |
 * | Technology):  "Wykorzystanie systemu wizyjnego do rozpoznawania   |
 * | emocji czlowieka" ("Vision system in human emotions recognition") |
 * | by Marcin Namysl in June 2008.                                    |
 * +-------------------------------------------------------------------+
 *
 * \author Marek Wnuk
 * \date 2009.02.26
 * \version 1.00.00
 */

/*
    FacET is a library for detecting and parameterising face components.
    Copyright (C) 2009  Marek Wnuk <marek.wnuk@pwr.wroc.pl>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*! \file timer.cpp
 *
 * File contains Timer class and functions for code execution timing.
 * 
 */

#include "timer.h"

using std::cerr;


#ifndef WIN32 

int Timer::_timer_ctr = 0; // GC

#endif // WIN32


#ifdef WIN32 

bool Timer::start() {  // all function is added NEW (GC)
  
  if (!tPerformanceTimerEnabled){
    LARGE_INTEGER time; // added NEW
    time.QuadPart = 0; // added NEW
    QueryPerformanceCounter(&time); // added NEW: get the current time and put it in time
    _start = ( float( (long int)time.QuadPart) * tResolution); //provides the computer current time in seconds; 
    _last = _start;
    tPerformanceTimerEnabled = true;
    return true;
  }
  else {
    cerr << "Error [class Timer; function start()] - " <<
      "Timer is running\n";
    return false;
  }
}

#else  // WIN32

bool Timer::start() { 
  if (!running){
    clock_gettime( CLOCK_REALTIME, &_start);
    _last = _start;
    running = true;
    return true;
  }
  else {
    cerr << "Error [class Timer; function start()] - " <<
      "Timer is running\n";
    return false;
  }
}

#endif // WIN32

#ifdef WIN32 

void Timer::stop() { // added NEW (GC)
  tPerformanceTimerEnabled = false;
}

#else  // WIN32

void Timer::stop() {
  if (running) running = false;
}

#endif // WIN32

#ifdef WIN32 

double Timer::readSec() { // all function is added NEW (GC)
  if (tPerformanceTimerEnabled) {
    
    LARGE_INTEGER time; // added NEW
    this->current_time.QuadPart = 0; // added NEW
    QueryPerformanceCounter(&this->current_time); // added NEW: get the current time and put it in time
    double curTime = ( float( (long int)this->current_time.QuadPart) * tResolution); //provides the computer current time in seconds; 
    double ttime = curTime - this->_last;
    this->_last = curTime;
    return ttime;
  }
  else {
    cerr << "Error [class Timer; function read()] - " << 
      "Timer is not running\n";
    return 0;
  }  
}

#else  // WIN32

double Timer::readSec() {
  if (running) {
    struct timespec current_time;
    clock_gettime( CLOCK_REALTIME, &current_time);   
    double ttime = (double)( current_time.tv_sec - _last.tv_sec ) + 
      (double)( current_time.tv_nsec - _last.tv_nsec)/BILLION;
    _last = current_time;
    return ttime;
  }
  else {
    cerr << "Error [class Timer; function read()] - " << 
      "Timer is not running\n";
    return 0;
  }  
}

#endif // WIN32

#ifdef WIN32 

double Timer::readSecSinceStart() { // all function added NEW (GC)
  if (tPerformanceTimerEnabled) {
    
    LARGE_INTEGER time; // added NEW
    time.QuadPart = 0; // added NEW
    QueryPerformanceCounter(&current_time); // added NEW: get the current time and put it in time
    double curTime = ( float( (long int)this->current_time.QuadPart) * tResolution); //provides the computer current time in seconds; 
    double ttime = curTime - this->_start;
    this->_last = curTime;
    return ttime;
  }
  else {
    cerr << "Error [class Timer; function read()] - " << 
      "Timer is not running\n";
    return 0;
  }  
}

#else  // WIN32

double Timer::readSecSinceStart() {
  if (running) {
    struct timespec current_time;
    clock_gettime( CLOCK_REALTIME, &current_time);   
    double ttime = (double)( current_time.tv_sec - _start.tv_sec ) + 
      (double)( current_time.tv_nsec - _start.tv_nsec)/BILLION;
    _last = current_time;
    return ttime;
  }
  else {
    cerr << "Error [class Timer; function read()] - " << 
      "Timer is not running\n";
    return 0;
  }  
}

#endif // WIN32

double Timer::readMSec(){
  return readSec()*1000;
}

double Timer::readMSecSinceStart(){
  return readSecSinceStart()*1000;
}

