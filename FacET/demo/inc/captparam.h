/* $Id: captparam.h 10 2009-03-24 21:45:48Z mw $
 * +-------------------------------------------------------------------+
 * | This file contains parts of code from the application created for |
 * | the Master thesis supervised by Marek Wnuk (Wroclaw University of |
 * | Technology):  "Wykorzystanie systemu wizyjnego do rozpoznawania   |
 * | emocji czlowieka" ("Vision system in human emotions recognition") |
 * | by Marcin Namysl in June 2008.                                    |
 * +-------------------------------------------------------------------+
 *
 * \author Marek Wnuk
 * \date 2009.03.13
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

/*! \file captparam.h
 *
 * The file includes the definitions of
 * \link ImageParameters camera image parameters\endlink 
 * used for image capturing setup.
 */

#ifndef _CAPTPARAM_H_
#define _CAPTPARAM_H_

/*! \class ImageParameters
 *
 *  Defines the image parameters (brightness, contrast, saturation and
 *  hue). Provides methods for parameters reading and setting.
 */
class ImageParameters
{
 protected:

  /*! Image width */
  int width;

  /*! Image height */
  int height;

  /*! Image brightness */
  int brightness;

  /*! Image contrast */
  int contrast;

  /*! Image saturation */
  int saturation;

  /*! Image hue */
  int hue;

  /*! Gain */
  int gain;

  /*! Horizontal image flipping
   *
   *  True - mirror-like image
   *  False - normal image
   */
  bool flip;

  /*! Image rotation by 180 deg.
   *
   *  True - seascape image
   *  False - normal image
   */
  bool rotate;

public:

  /*! Parameterless constructor
   * 
   *  Sets the default values of the components
   */
  ImageParameters(): width(640), height(480), brightness(50), contrast(13), 
    saturation(13), hue(13), gain(30), flip(false), rotate(false) {
  }
    
    /*! Constructor
     *
     *  Sets the provided values of the components
     *
     * \param wdth - image width value
     * \param hght - image height value
     * \param b - brightness value
     * \param c - contrast value
     * \param s - saturation value
     * \param h - hue value
     * \param g - gain value
     * \param f - flip value
     * \param r - rotate value
     */
    ImageParameters(int wdth, int hght, int b, int c, int s, int h, int g, 
      bool f, bool r): 
      width(wdth), height(hght), brightness(b), contrast(c), saturation(s), 
      hue(h), gain(g), flip(f), rotate(r) {}
      
      /*! Sets the image parameters 
       *  
       * \param wdth - new image width value
       * \param hght - new image height value
       * \param b - new brightness value
       * \param c - new contrast value
       * \param s - new saturation value
       * \param h - new hue value
       * \param g - new gain value
       * \param f - new flip value
       * \param r - new rotate value
       */
      void setParameters(int wdth, int hght, int b, int c, int s, int h, int g, 
        bool f, bool r) {
	height=hght; width=wdth; brightness=b; contrast=c; saturation=s; 
	hue=h; gain=g; flip=f; rotate=r;
      }
      
      /*! Sets the image width value
       *
       *  \param val - new image width value
       */
      void setWidth(int val) { width = val; }
      
      /*! Reads the image width value
       *
       *  \return image width value
       */
      int readWidth() const { return width; }
      
      /*! Sets the image height value
       *
       *  \param val - new image height value
       */
      void setHeight(int val) { height = val; }
      
      /*! Reads the image height value
       *
       *  \return image height value
       */
      int readHeight() const { return height; }
      
      /*! Sets the brightness value
       *
       *  \param val - new brightness value
       */
      void setBrightness(int val) { brightness = val; }
      
      /*! Reads the brightness value
       *
       *  \return brightness value
       */
      int readBrightness() const { return brightness; }
      
      /*! Sets the contrast value
       *
       *  \param val - new contrast value
       */
      void setContrast(int val) { contrast = val; }
      
      /*! Reads the contrast value
       *
       *  \return contrast value
       */
      int readContrast() const { return contrast; }
      
      /*! Sets the saturation value
       *
       *  \param val - new saturation value
       */
      void setSaturation(int val) { saturation = val; }
      
      /*! Reads the saturation value
       *
       *  \return saturation value
       */
      int readSaturation() const { return saturation; }
      
      /*! Sets the hue value
       *
       *  \param val - new hue value
       */
      void setHue(int val) { hue = val; }
      
      /*! Reads the hue value
       *
       *  \return hue value
       */
      int readHue() const { return hue; }
      
      /*! Sets the hue value
       *
       *  \param val - new hue value
       */
      void setGain(int val) { gain = val; }
      
      /*! Reads the hue value
       *
       *  \return hue value
       */
      int readGain() const { return gain; }
      
      /*! Sets the value of \link ImageParameters::flip flip\endlink flag
       * 
       * \param val - if 0 then flip is set to FALSE, otherwise
       *              flip is set to TRUE
       */
      void setFlip(int val) { val!=0 ? flip=true : flip=false; }
      
      /*! Reads the value of flip flag
       *
       * \return value of flip flag
       */
      bool readFlip() { return flip; }
      
      /*! Sets the value of \link ImageParameters::rotate rotate\endlink 
       * flag
       * 
       * \param val - if 0 then rotate is set to FALSE, otherwise
       *              rotate is set to TRUE
       */
      void setRotate(int val) { val!=0 ? rotate=true : rotate=false; }
      
      /*! Reads the value of rotate flag
       *
       * \return value of rotate flag
       */
      bool readRotate() { return rotate; }

};

#endif
