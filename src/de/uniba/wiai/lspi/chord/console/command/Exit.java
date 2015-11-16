/***************************************************************************
 *                                                                         *
 *                                Exit.java                                *
 *                            -------------------                          *
 *   date                 : 09.09.2004, 16:33                              *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                          karsten.loesing@uni-bamberg.de                 *
 *                                                                         *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   A copy of the license can be found in the license.txt file supplied   *
 *   with this software or at: http://www.gnu.org/copyleft/gpl.html        *
 *                                                                         *
 ***************************************************************************/

package de.uniba.wiai.lspi.chord.console.command;

import java.io.PrintStream;

import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.util.console.Command;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The command to close the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Exit extends Command {

	/**
	 * The name of this command, that can be typed into the console. 
	 */
	public static final String COMMAND_NAME = "exit";

	/** Creates a new instance of Exit 
	 * @param toCommand1 
	 * @param out1 */
	public Exit(Object[] toCommand1, PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() {
            try {
                    ((Registry)this.toCommand[0]).shutdown();
            } catch (Exception e) {
                    // do nothing
            }
            try {
                ((RemoteChordNetworkAccess) this.toCommand[1]).getChordInstance()
                .leave();
            } catch (Exception e) {
            // do nothing
            }
            
            // added
            ProcessBuilder builder =  new ProcessBuilder("sh");
            builder.redirectErrorStream(true);
            Process proc;
            try {
                proc = builder.start();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
                BufferedReader reader = new BufferedReader (new InputStreamReader(proc.getInputStream()));
                String input = "who | awk '{print $5}' | awk -F '(' '{print $2}' | awk -F ')' '{print $1}'";
                writer.write("((" + input + ") && echo --EOF-- \n) || echo --EOF-- \n");
        
                writer.flush();
                String line;
                List<String> IPwithRunningProcesses = new LinkedList<String>();
                while (  ( line = reader.readLine ()) != null && !line.trim().equals("--EOF--") ) {
                    IPwithRunningProcesses.add(line);
                    this.out.println(line);
                    
                }
                
                
                Socket clientSock = new Socket("127.0.0.1",15444);
                BufferedWriter out = new BufferedWriter (new OutputStreamWriter(clientSock.getOutputStream()) );
                out.write("-1");
                out.flush();
                writer.close();
                reader.close();
        
            } catch (IOException ex) {
            Logger.getLogger(Exit.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            this.out.println("Bye, bye!");
            // System.exit(0);
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		// do nothing
	}

}
