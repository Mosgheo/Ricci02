package vertxImpl;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.*;

import org.graphstream.graph.Graph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import io.vertx.core.eventbus.EventBus;

public class Gui extends JFrame {

	private static final long serialVersionUID = 1L;
	private Container mainContainer;
	private JPanel componentPane;
	private JPanel p1;
	private JPanel p2;
	private JPanel graphPanel;
	private JLabel labelUrl;
	private JLabel labelDepth;
	private JTextField urlText;
	private JTextField depthText;
	private JButton run;
	private JLabel nodeCounts;
	@SuppressWarnings("unused")
	private EventBus eb;
	private Graph graph;
	private JFrame myFrame;
	/**
	 * Creates a view of the specified size (in pixels)
	 * 
	 * @param w
	 * @param h
	 */
	public Gui(int w, int h, final EventBus eb, Graph g) {

		setTitle("Graph Simulation");
		setSize(w, h);
		myFrame = new JFrame();
		myFrame.setSize(new Dimension(w, h));
		
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);
		this.eb = eb;
		this.graph=g;
		mainContainer = new JPanel();
		componentPane = new JPanel();
		p1 = new JPanel();
		p2 = new JPanel();
		graphPanel = new JPanel(new GridLayout()){
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public Dimension getPreferredSize() {
                return new Dimension(920, 540);
            }
        };
		
		SpringLayout layout = new SpringLayout();
		mainContainer.setLayout(layout);
		mainContainer.add(componentPane);
		mainContainer.add(p1);
		mainContainer.add(p2);
		mainContainer.add(graphPanel);
		
		nodeCounts = new JLabel("Total nodes: ");
		urlText = new JTextField("");
		urlText.setPreferredSize(new Dimension(150,30));
		depthText = new JTextField("1");
		depthText.setPreferredSize(new Dimension(150,30));
		run = new JButton("Run");
		labelUrl = new JLabel("Insert an URL");
		labelDepth = new JLabel("Insert a Depth");
		
		run.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!isNumeric(depthText.getText())) {
					JOptionPane.showMessageDialog(myFrame, "Insert a valid number");
				} else {
						if (isURL(urlText.getText())) {
							String url = getContent(urlText.getText());
							int dept = Integer.parseInt(depthText.getText());
							String message = url + ":"+dept;
							eb.send("init",message);
						} else {
							JOptionPane.showMessageDialog(myFrame, "Error 404, insert a valid URL");
						}
				}
			}
		});
		
		layout.putConstraint(SpringLayout.NORTH, componentPane, 0, SpringLayout.NORTH, mainContainer);
		layout.putConstraint(SpringLayout.WEST, componentPane , 0, SpringLayout.WEST, mainContainer);
		
		layout.putConstraint(SpringLayout.WEST, p1, 0, SpringLayout.WEST, mainContainer);
		
		layout.putConstraint(SpringLayout.SOUTH, p1, 0, SpringLayout.NORTH, p2);
		
		layout.putConstraint(SpringLayout.WEST, p2, 0, SpringLayout.WEST, mainContainer);
		layout.putConstraint(SpringLayout.SOUTH, p2, 0, SpringLayout.SOUTH, mainContainer);

		layout.putConstraint(SpringLayout.WEST, graphPanel, 20, SpringLayout.WEST, mainContainer);
		layout.putConstraint(SpringLayout.NORTH, graphPanel, 20, SpringLayout.SOUTH, componentPane );
		layout.putConstraint(SpringLayout.NORTH, p1, 10, SpringLayout.SOUTH, graphPanel);
		
		Viewer graphviewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		graphviewer.enableAutoLayout();
        ViewPanel viewPanel = graphviewer.addDefaultView(false);
        
        //add a mouse wheel listener to the ViewPanel for zooming the graph
        viewPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                zoomGraphMouseWheelMoved(mwe, viewPanel);
            }
        });
        
        
        graphPanel.add(viewPanel);
        
		componentPane.add(labelUrl);
		componentPane.add(urlText);
		componentPane.add(nodeCounts);
		
		p1.add(labelDepth);
		p1.add(depthText);
		p2.add(run);
		
		myFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(-1);
				eb.send("stop", "");
			}

			public void windowClosed(WindowEvent ev) {
				System.exit(-1);
			}
		});
		myFrame.add((Component) view);
		myFrame.add(mainContainer);
	}
	public void display() {
		myFrame.setVisible(true);
	}
	private static String getContent(final String link) {
		String content = link.substring(30);
		//System.out.println(basicUrl+"w/api.php?action=parse&page=" + content + "&format=json&section=0&prop=links");
		// System.out.println(basicUrl);
		//System.out.println(content);
		return content;
	}
	
	 private void zoomGraphMouseWheelMoved(MouseWheelEvent mwe, ViewPanel view_panel){
	        if (Event.ALT_MASK != 0) {            
	            if (mwe.getWheelRotation() > 0) {
	                double new_view_percent = view_panel.getCamera().getViewPercent() + 0.05;
	                view_panel.getCamera().setViewPercent(new_view_percent);               
	            } else if (mwe.getWheelRotation() < 0) {
	                double current_view_percent = view_panel.getCamera().getViewPercent();
	                if(current_view_percent > 0.05){
	                    view_panel.getCamera().setViewPercent(current_view_percent - 0.05);                
	                }
	            }
	        }                     
	    }
	
	private static boolean isNumeric(String str) { 
	  try {  
	    Integer.parseInt(str);  
	    return true;
	  } catch(NumberFormatException e) {  
	    return false;  
	  }  
	}
	public void updateLabel() {
		this.nodeCounts.setText(String.valueOf(graph.getNodeCount())); 
	}
	private boolean isURL(String url) {
	  try {
	     (new URL(url)).openStream().close();
	     return true;
	  } catch (Exception ex) { }
	  return false;
	}
}
