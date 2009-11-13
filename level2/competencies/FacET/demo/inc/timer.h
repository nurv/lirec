/* $Id: timer.h 5 2009-03-12 22:30:56Z mw $
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

/*! \file timer.h
 *
 * File contains Timer class and functions for code execution timing.
 */

#ifndef _TIMER_H_
#define _TIMER_H_

#include <iostream>

#ifdef WIN32 
#include "windows.h" // GC
#endif // WIN32

/*! One billion (10^9) constant */
#define BILLION 1000000000

/*! \class Timer
 *
 * Used for code sections timing by starting the timer and reading 
 * its value in appropriate points of code.
 * Both milliseconds and seconds reads are provided.
 */
class Timer {

private:  

#ifdef WIN32

  float _start; // added NEW (GC)

  float _last; // added NEW (GC)

  LARGE_INTEGER current_time; // added NEW (GC)

  bool tPerformanceTimerEnabled; // added NEW (GC)

  long int tFrequency; // added NEW (GC)

  double tResolution; // added NEW (GC)

#else  // WIN32

  /*! Number of started timers */
  static int _timer_ctr;   

  /*! Time counting start */
  struct timespec  _start;

  /*! Time measurement performed lately */
  struct timespec  _last;

  /*! Timer running flag */
  bool running;

#endif // WIN32

public:

  /*! Parameterless constructor */
#ifdef WIN32

  Timer() { tPerformanceTimerEnabled = false;
    QueryPerformanceFrequency((LARGE_INTEGER *) &this->tFrequency); // added NEW
    this->tResolution = ((double)1.0)/((double)this->tFrequency); // added NEW
  }// added NEW (GC) 
  
#else  // WIN32

  Timer() { running=false; _timer_ctr++; }

#endif // WIN32

  /*! Starts time measurement unless timer is running.
   *
   * \retval true if the timer was idle
   * \retval false if timer was running
   */
  bool start();

  /*! Stops time measurement and sets \link Timer::running running\endlink
   * value to false
   */
  void stop();

  /*! Reads number of seconds since last read.
   *
   * \return number of seconds since last read
   */
  double readSec();

  /*! Reads number of seconds since timer start.
   *
   * \return number of seconds since timer start
   */
  double readSecSinceStart();

  /*! Reads number of milliseconds since last read.
   *
   * \return number of milliseconds since last read
   */
  double readMSec();

  /*! Reads number of milliseconds since timer start.
   *
   * \return number of milliseconds since timer start
   */
  double readMSecSinceStart();
};

#endif
