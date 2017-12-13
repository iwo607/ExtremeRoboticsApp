package gui;

import threads.DownloaderThread;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import threads.FileProcessingThread;

import javax.swing.*;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Primary application class, containing execution command and frontend elements.
 */
public class App
{
    public JPanel mainPanel;
}
