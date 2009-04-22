/* $Id: options.h 5 2009-03-12 22:30:56Z mw $
 *
 * \author Marek Wnuk
 * \date 2009.03.03
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

/*! \file options.h
 *
 * The file includes the definitions of 
 * \link DetectionOptions user application options\endlink
 * 
 */

#ifndef _OPTIONS_H_
#define _OPTIONS_H_

/*! \class DetectionOptions
 *
 *  Defines the options for face features detection procedures (timing,
 *  recording, verbosity).
 *  Provides methods for options reading and setting.
 */
class DetectionOptions
{
 protected:

  /*! Detection procedures timing */
  bool timing;

  /*! Image/video recording */
  bool imgrec;

  /*! Parameters recording */
  bool parrec;

  /*! Image/video results display */
  bool show;

  /*! Verbosity */
  int verbose;

  /*! Output image path name */
  std::string output_image_path;

public:

  /*! Parameterless constructor
   * 
   *  Sets the default values of the components
   */
  DetectionOptions(): timing(false), imgrec(true), parrec(true), show(true), 
    verbose(1) {
    output_image_path = "output/";
  }
    
    /*! Constructor
     *
     *  Sets the provided values of the components
     *
     * \param tf - procedures timing flag
     * \param ir - image recording flag
     * \param pr - parameters recording flag
     * \param sh - output display flag
     * \param v  - verbosity value
     */
    DetectionOptions(bool tf, bool ir, bool pr, bool sh, int v): 
      timing(tf), imgrec(ir), parrec(pr), show(sh), verbose(v) {}
      
      /*! Sets the detection options 
       *  
       * \param tf - new procedures timing flag
       * \param ir - new image recording flag
       * \param pr - new parameters recording flag
       * \param sh - new output display flag
       * \param v  - new verbosity value
       */
      void setOptions(bool tf, bool ir, bool pr, bool sh, int v) {
	timing=tf; imgrec=ir; parrec=pr; show = sh; verbose=v;
      }
      
      /*! Sets the value of \link DetectionOptions::timing timing\endlink 
       * flag
       * 
       * \param val - if 0 then timing is set to FALSE, otherwise
       *              timing is set to TRUE
       */
      void setTiming(int val) { val!=0 ? timing=true : timing=false; }
      
      /*! Reads the value of timing flag
       *
       * \return value of timing flag
       */
      bool readTiming() { return timing; }

      /*! Sets the value of \link DetectionOptions::imgrec imgrec\endlink 
       * flag
       * 
       * \param val - if 0 then imgrec is set to FALSE, otherwise
       *              imgrec is set to TRUE
       */
      void setImgrec(int val) { val!=0 ? imgrec=true : imgrec=false; }
      
      /*! Reads the value of imgrec flag
       *
       * \return value of imgrec flag
       */
      bool readImgrec() { return imgrec; }

      /*! Sets the value of \link DetectionOptions::parrec parrec\endlink 
       * flag
       * 
       * \param val - if 0 then parrec is set to FALSE, otherwise
       *              parrec is set to TRUE
       */
      void setParrec(int val) { val!=0 ? parrec=true : parrec=false; }
      
      /*! Reads the value of parrec flag
       *
       * \return value of parrec flag
       */
      bool readParrec() { return parrec; }

      /*! Sets the value of \link DetectionOptions::show show\endlink 
       * flag
       * 
       * \param val - if 0 then show is set to FALSE, otherwise
       *              show is set to TRUE
       */
      void setShow(int val) { val!=0 ? show=true : show=false; }
      
      /*! Reads the value of show flag
       *
       * \return value of show flag
       */
      bool readShow() { return show; }

      /*! Sets the verbosity value
       *
       *  \param val - new verbosity value
       */
      void setVerbose(int val) { verbose = val; }
      
      /*! Reads the verbosity value
       *
       *  \return image verbosity value
       */
      int readVerbose() const { return verbose; }


      /*! Sets the output image path
       *
       *  \param file_name - output image path
       */
      void setOutputImagePath(std::string file_name) { 
	output_image_path = file_name;
      }
      
      /*! Reads the output image path
       *
       *  \return file_name - output image path
       */
      std::string readOutputImagePath() const { 
	return output_image_path; 
      }
};

#endif
