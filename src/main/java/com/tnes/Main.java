package com.tnes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.tnes.NES.IPowerOffHandler;
import com.tnes.NES.IPowerOnHandler;
import com.tnes.NES.IROMLoadHandler;
import com.tnes.NES.NESEvent;
import com.tnes.NES.PowerOffEvent;
import com.tnes.NES.PowerOnEvent;
import com.tnes.NES.ROMLoadEvent;

public class Main {
	private static final long serialVersionUID = 1L;

	public Main() {
	}

	public static void main(String[] args) {
		final NES nes = new NES();
		final Debugger debugger = Debugger.getInstance();
		boolean debugging = false;

		final ResourceBundle resourceBundle = ResourceBundle.getBundle("tnes");
		final JFrame window = new JFrame(resourceBundle.getString("com.tnes.windowTitle"));
		final JMenuBar menuBar = new JMenuBar();
		final JMenu system = new JMenu(resourceBundle.getString("com.tnes.menu.system"));
		final JMenuItem loadROM = new JMenuItem(resourceBundle.getString("com.tnes.menu.system.loadROM"));
		final JCheckBoxMenuItem power = new JCheckBoxMenuItem(resourceBundle.getString("com.tnes.menu.system.power"));
		final JMenuItem reset = new JMenuItem(resourceBundle.getString("com.tnes.menu.system.reset"));

		/*
		 * Build the window
		 */
		window.setSize(256, 321);
		window.setVisible(true);
		window.setLayout(new BorderLayout());

		/*
		 * Drop the user back into the debugger if the window is closed. TODO:
		 * This doesn't ever seem to get called.
		 */
		window.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if (e.getNewState() == WindowEvent.WINDOW_CLOSED) {
					// Drop into the debugger, if debugging
					if (debugger.isDebugging()) {
						debugger.readCommands();
					} else {
						System.exit(0);
					}
				}
			}
		});

		// Build the menu bar
		window.setJMenuBar(menuBar);

		loadROM.addActionListener(new Main.NESAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nes.powerOff();

				JFileChooser fileChooser = new JFileChooser(new File("."));

				int rVal = fileChooser.showOpenDialog(window);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					File romFile = fileChooser.getSelectedFile();
					String sROMFilePath = romFile.getAbsolutePath();
					if (sROMFilePath != null && !sROMFilePath.isEmpty()) {
						if (nes.isPoweredOn())
							nes.powerOff();

						nes.loadROM(sROMFilePath);
					}
				}

			}
		});
		loadROM.setText(resourceBundle.getString("com.tnes.menu.system.loadROM"));

		power.addItemListener(new Main.NESToggleAction() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED && !nes.isPoweredOn()) {
					nes.powerOn();
				} else if (e.getStateChange() == ItemEvent.DESELECTED && nes.isPoweredOn()) {
					nes.powerOff();
				}
			}
		});
		power.setText(resourceBundle.getString("com.tnes.menu.system.power"));

		reset.addActionListener(new Main.NESAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nes.reset();
			}
		});
		reset.setText(resourceBundle.getString("com.tnes.menu.system.reset"));

		// Build the system menu
		system.add(loadROM);
		system.add(power);
		system.add(reset);
		menuBar.add(system);

		// Build Processing screen
		Screen screen = new Screen(nes);
		window.add(screen, BorderLayout.CENTER);
		screen.init();

		/*
		 * Add NES handlers
		 */
		nes.addHandler(PowerOnEvent.class, new IPowerOnHandler() {
			public void handleEvent(NESEvent e) {
				if (!power.isSelected()) {
					power.setSelected(true);

				}
			}
		});

		nes.addHandler(PowerOffEvent.class, new IPowerOffHandler() {
			public void handleEvent(NESEvent e) {
				if (power.isSelected())
					power.setSelected(false);
			}
		});

		nes.addHandler(ROMLoadEvent.class, new IROMLoadHandler() {
			public void handleEvent(NESEvent e) {
				String romFile = nes.getROMFile().getName();
				window.setTitle(resourceBundle.getString("com.tnes.windowTitle") + ": " + romFile);
			}
		});

		/* Read command line args and kick things off */
		String romFile = "";
		for (String arg : args) {
			if (arg.matches("[^\\.]*\\.nes")) {
				romFile = arg;
			} else if (arg.matches("-[dD]")) {
				debugging = true;
			}
		}

		debugger.setDebugging(debugging);

		if (!romFile.isEmpty()) {
			nes.loadROM(romFile);
			nes.powerOn();
		}
	}

	public static class NESAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
		}
	}

	public static class NESToggleAction implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
		}
	}
}
