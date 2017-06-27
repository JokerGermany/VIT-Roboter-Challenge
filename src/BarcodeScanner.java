/*
 *  Copyright 2008, 2010 Guenther Hoelzl
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import lejos.nxt.*;
import lejos.nxt.comm.*;

/**
 * This class is for introduction to programming the Lego NXT roboter
 * in Java and was originally constructed for the seminar DS82221.
 * It's a barcodescanner with the Mindstorms NXT-Robot for reading
 * EAN-13 Barcodes.
 * @author Guenther Hoelzl
 * @version 1.0
 */
class BarcodeScanner {
    /**
     * method for converting a line pattern to a digit
     * @param value decimal value of four line lengths
     * @return found digit*2 + bit for calculating the first digit
     */
    public static int findDigit(int value) {
        final int [] DIGITCODES = {
            3211, 1123,
            2221, 1222,
            2122, 2212,
            1411, 1141,
            1132, 2311,
            1231, 1321,
            1114, 4111,
            1312, 2131,
            1213, 3121,
            3112, 2113
        };

        for (int i=0; i<DIGITCODES.length; i++)
            if (value == DIGITCODES[i])
                return i;

        // no digit found -> Error
        return -1;
    }

    /**
     * method for decoding the first digit from the first 6 codes
     * @param value binary value of the first six code-possibilities
     * @return first digit
     */
    public static int decodeFirstDigit(int value) {
        final int [] DIGITCODES = {
            0,
            11,
            13,
            14,
            19,
            25,
            28,
            21,
            22,
            26
        };

        for (int i=0; i<DIGITCODES.length; i++)
            if (value == DIGITCODES[i])
                return i;

        // no digit found -> Error
        return -1;
    }

    /**
     * Starting method
     * @param args not used
     */
    public static void main (String args[]) {
        int actDigit;
        int lineValue = 0;
        int firstDigit;
        int displayLine=0;

        // calibrate the helper thread before starting to achieve more accurate results
        LineReader myLineReader = new LineReader();
        myLineReader.calibrate();
        // init display and start vehicle
        LCD.clear();
        myLineReader.start();
        Motor.A.setSpeed(250);
        Motor.C.setSpeed(250);
        Motor.A.forward();
        Motor.C.forward();

        while (true) {
            // init calculations
            firstDigit = 0;
            // wait for a new barcode, the code consists of two 3-length-sync-lines,
            // one 5-length-sync-line and 2*6 digit-lines
            myLineReader.readSyncLines();
            Sound.beep();
            myLineReader.waitBarcode(3+5+2*6*4, 3+5+2*6*7);

            // read the first 6 digits
            for (int number = 0; number < 6; number++) {
                lineValue = 0;

                for (int i=0; i<4; i++)
                    lineValue = lineValue*10+myLineReader.readNormalizedLineLength();

                if ((actDigit=findDigit(lineValue))>=0) {
                    LCD.drawInt(actDigit>>1, number+2, displayLine);
                    firstDigit = (firstDigit<<1) | (actDigit & 1);
                }
            }

            // read the middle sync-lines
            myLineReader.readLines(5);

            // read the last 6 digits
            for (int number = 0; number < 6; number++) {
                lineValue = 0;

                for (int i=0; i<4; i++)
                    lineValue = lineValue*10+myLineReader.readNormalizedLineLength();

                if ((actDigit=findDigit(lineValue))>=0)
                    LCD.drawInt(actDigit>>1, number+9, displayLine);
            }

            // read the last sync-lines and display the calculated first digit
            myLineReader.readLines(3);
            LCD.drawInt(decodeFirstDigit(firstDigit), 0, displayLine);
            LCD.refresh();
            Sound.beep();

            if (++displayLine == 8)
                displayLine = 0;
        }
    }
}


/**
 * this class is a helper thread for parallel reading the light sensor and
 * calculating the actual line widths
 * @author Guenther Hoelzl
 * @version 1.0
 */
class LineReader extends Thread {
    final static int MAXLINES = 64;
    //final static int THRESHOLD = 275; // this is the average between white and black in most situations

    int[] lineLength = new int[MAXLINES]; // circular buffer
    int writePos;
    int readPos;
    boolean readerWaiting;
    double averageTime;
    int threshold;

    /**
     * constructor of the class
     */
    public LineReader() {
        writePos = 0;
        readPos = 0;
        readerWaiting = false;
    }

    /**
     * calibrates the light sensor for achieving better results
     */
    public void calibrate() {
        LightSensor light = new LightSensor(SensorPort.S1);
        LCD.clear();

        // wait for unpressed ENTER-Key and measure threhold
        while (Button.ENTER.isPressed());

        do {
            threshold = light.readNormalizedValue();
            LCD.drawString("threshold="+threshold,0,0);
            LCD.refresh();
        } while (!Button.ENTER.isPressed());

        // wait for unpressed ENTER-Key and return
        while (Button.ENTER.isPressed());

        LCD.drawString("press ENTER!",0,1);
        LCD.refresh();

        while (!Button.ENTER.isPressed());
    }

    /**
     * @return measured line length divided by the average time for reading a single 1-line
     */
    public int readNormalizedLineLength() {
        return (int)(readLineLength()/averageTime + 0.5);
    }

    /**
     * wait until the given number of lines is detected
     * @param lines
     */
    public void readLines(int lines) {
        for (int i=0; i<lines; i++)
            readLineLength();
    }

    /**
     * method with a simple algorithm for detecting 3 almost equal sized lines
     */
    public void readSyncLines() {
        int time1=0;
        int time2=0;
        int time3=0;
        double average;
        double simpleNormVariance;

        while (true) {
            if (time3 != 0) {
                // calculate "simple" normalized variance between time-values
                // Problem with Math.abs!!!
                average = (time1 + time2 + time3)/3.0;
                simpleNormVariance = (time3 - time1)/average;

                if ((simpleNormVariance>-0.5) && (simpleNormVariance<0.5))
                    return;
            }

            time3 = time2;
            time2 = time1;
            time1 = readLineLength();
        }
    }

    /**
     * waits until the given amount of lines is detected and calulates
     * the average simple line time
     * @param lines actual number of lines
     * @param times number of 1-unit-lines
     */
    public void waitBarcode(int lines, int times) {
        int endWritePos = (lines + readPos)%lineLength.length;

        synchronized (lineLength) {
            while (endWritePos != writePos) {
                readerWaiting = true;

                try {
                    lineLength.wait();

                } catch (InterruptedException e) {}

                readerWaiting = false;
            }
        }

        int pos=readPos;
        int sum = 0;

        while (pos != endWritePos) {
            sum += lineLength[pos];
            pos = (pos+1)%lineLength.length;
        }

        averageTime = (double) sum / (double) times;
    }

    /**
     * flush the read/write-buffer
     */
    public void flushBuffer() {
        synchronized (lineLength) {
            readPos = writePos;
        }
    }

    /**
     * @return number from the read/write-buffer
     */
    private int readLineLength() {
        int returnValue;

        synchronized (lineLength) {
            if (readPos == writePos) {
                readerWaiting = true;

                try {
                    lineLength.wait();

                } catch (InterruptedException e) {}

                readerWaiting = false;
            }
        }

        returnValue = lineLength[readPos];
        readPos = (readPos+1)%lineLength.length;
        return returnValue;
    }

    /**
     * @param length number to write
     */
    private void writeLineLength(int length) {
        lineLength[writePos] = length;

        synchronized (lineLength) {
            writePos = (writePos+1)%lineLength.length;

            if (readerWaiting)
                lineLength.notify();
        }
    }

    /**
     * threads never ending loop for continously reading barcode-lines
     */
    public void run() {
        int actValue;
        boolean isBlack = false;
        int actTime = 0;

        // switch on lightsensor and wait some time for stabilization
        LightSensor light = new LightSensor(SensorPort.S1);

        try {
            Thread.sleep(500);

        } catch (InterruptedException e) {}

        // read sensorvalue and compare with the calculated threshold (Schmitt-trigger)
        // store timedifference in private array
        // without sleep: needs just 0.166 ms for one iteration
        while (true) {
            actValue = light.readNormalizedValue();

            if (isBlack) {
                if (actValue >= (threshold+5)) {
                    writeLineLength(actTime);
                    isBlack = false;
                    actTime = 0;
                }

            } else {
                if (actValue <= (threshold-5)) {
                    writeLineLength(actTime);
                    isBlack = true;
                    actTime = 0;
                }
            }

            try {
                Thread.sleep(2);

            } catch (InterruptedException e) {}

            actTime++;
        }
    }
}