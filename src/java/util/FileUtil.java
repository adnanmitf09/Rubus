package util;
/*
 * Rubus: A Compiler for Seamless and Extensible Parallelism
 * 
 * Copyright (C) 2017 Muhammad Adnan - University of the Punjab
 * 
 * This file is part of Rubus.
 * Rubus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.

 * Rubus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Rubus. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.Desktop;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

public class FileUtil {

	public static void openFile(final File source) {
		TimerTask openFile = new TimerTask() {

			@Override
			public void run() {
				try {
					Desktop.getDesktop().open(source);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							null,
							"Unable to open source file :  "
									+ source.getAbsolutePath());
				}
			}
		};

		new Timer().schedule(openFile, 0);
		
	}

}
