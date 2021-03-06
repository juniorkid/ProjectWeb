import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.regex.*;
import javax.swing.border.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.treetable.*;

import org.apache.commons.lang3.*;

class EmU_UserAndAdminManagement extends JDialog implements ConstantVars
{
	private static final long serialVersionUID = -1213582265865921754L;

	// Declaration of the Native C functions
	private native void uninit_backend();
	private native boolean register_user_main(boolean is_admin_flag, String username, String email_address);
	private native boolean edit_user_email_address_main(boolean is_admin_flag, String username, String email_address);

	// Variables
	private JPanel     main_panel                 = new JPanel();

	private JTextField username_textfield         = new JTextField(TEXTFIELD_LENGTH);
	private JTextField email_address_textfield    = new JTextField(TEXTFIELD_LENGTH);

	private JButton    submit_button;
	private JButton    cancel_button              = new JButton("Cancel");

	private boolean    is_registration_mode_flag;     // Registration or editing mode
	private boolean    is_admin_flag;

	// This for editing mode only
	private String     current_email_address;

	// Return variable
	private boolean    result_flag;

	public EmU_UserAndAdminManagement(Component parent, boolean is_admin_flag)       // Registration mode
	{
		result_flag               = false;
		is_registration_mode_flag = true;
		this.is_admin_flag        = is_admin_flag;

		// Load JNI backend library
		System.loadLibrary("PHRapp_EmU_Admin_JNI");
			
		init_ui(parent);
		setup_actions();
	}

	public EmU_UserAndAdminManagement(Component parent, boolean is_admin_flag, String username, String current_email_address)       // Editing mode
	{
		result_flag                = false;
		is_registration_mode_flag  = false;
		this.is_admin_flag         = is_admin_flag;
		this.current_email_address = current_email_address;

		// Load JNI backend library
		System.loadLibrary("PHRapp_EmU_Admin_JNI");
			
		init_ui(parent);
		init_textfields(username, current_email_address);
		setup_actions();
	}

	private final void init_ui(Component parent)
	{
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JLabel username_label      = new JLabel("Username: ", JLabel.RIGHT);
		JLabel email_address_label = new JLabel("E-mail address: ", JLabel.RIGHT);

		JPanel upper_inner_panel = new JPanel(new SpringLayout());
		upper_inner_panel.add(username_label);
		upper_inner_panel.add(username_textfield);
		upper_inner_panel.add(email_address_label);
		upper_inner_panel.add(email_address_textfield);

		SpringUtilities.makeCompactGrid(upper_inner_panel, 2, 2, 5, 10, 10, 10);

		JPanel upper_outer_panel = new JPanel();
		upper_outer_panel.setLayout(new BoxLayout(upper_outer_panel, BoxLayout.X_AXIS));
		upper_outer_panel.setPreferredSize(new Dimension(400, 80));
		upper_outer_panel.setMaximumSize(new Dimension(400, 80));
		upper_outer_panel.setAlignmentX(0.0f);
		upper_outer_panel.add(upper_inner_panel);

		// Buttons
		submit_button = (is_registration_mode_flag) ? new JButton("Register") : new JButton("Edit");
		submit_button.setAlignmentX(0.5f);
		cancel_button.setAlignmentX(0.5f);

		JPanel buttons_panel = new JPanel();
		buttons_panel.setPreferredSize(new Dimension(400, 30));
		buttons_panel.setMaximumSize(new Dimension(400, 30));
		buttons_panel.setAlignmentX(0.0f);
		buttons_panel.add(submit_button);
		buttons_panel.add(cancel_button);

		// Main panel
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
		main_panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

		main_panel.add(upper_outer_panel);
		main_panel.add(buttons_panel);

		setModalityType(ModalityType.APPLICATION_MODAL);
		add(main_panel);

		setTitle(((is_admin_flag) ? "Admin" : "User") + ((is_registration_mode_flag) ? " Registration" : " Editing"));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
		setResizable(false);
	}

	private final void setup_actions()
	{
		// Submit button
		submit_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
		    			public void run()
					{
						submit_button.setEnabled(false);
						cancel_button.setEnabled(false);

						String username      = username_textfield.getText();
						String email_address = email_address_textfield.getText();

						if(is_registration_mode_flag && validate_input_registration_mode())
						{
							// Call to C function
							if(register_user_main(is_admin_flag, username, email_address))
							{
								result_flag = true;
		        					dispose();
							}
						}
						else if(!is_registration_mode_flag && validate_input_editing_mode())
						{
							// Call to C function
							if(edit_user_email_address_main(is_admin_flag, username, email_address))
							{
								result_flag = true;
		        					dispose();
							}
						}

						submit_button.setEnabled(true);
						cancel_button.setEnabled(true);
		    			}
				});
			}
		});

		// Cancel button
		cancel_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
		    			public void run()
					{
						submit_button.setEnabled(false);
						cancel_button.setEnabled(false);
		        			dispose();
					}
				});
		    	}
		});
	}

	private void init_textfields(String username, String email_address)
	{
		username_textfield.setText(username);
		username_textfield.setEnabled(false);

		email_address_textfield.setText(email_address);
	}

	private boolean validate_input_registration_mode()
	{
		Pattern p;
		Matcher m;

		// Validate username
		p = Pattern.compile("^[^-]*[a-zA-Z0-9_]+");

		m = p.matcher(new String(username_textfield.getText()));
		if(!m.matches())
		{
			JOptionPane.showMessageDialog(this, "Please input correct format for the username");
			return false;
		}

		// Validate e-mail address
		p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

		m = p.matcher(new String(email_address_textfield.getText()));
		if(!m.matches())
		{
			JOptionPane.showMessageDialog(this, "Please input correct format for the email address");
			return false;
		}

		return true;
	}

	private boolean validate_input_editing_mode()
	{
		Pattern p;
		Matcher m;
		String  email_address = email_address_textfield.getText();

		// Validate e-mail address
		p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

		m = p.matcher(email_address);
		if(!m.matches())
		{
			JOptionPane.showMessageDialog(this, "Please input correct format for the email address");
			return false;
		}

		// Check update
		if(email_address.equals(current_email_address))
		{
			JOptionPane.showMessageDialog(this, "No any update");
			return false;
		}

		return true;
	}

	public boolean get_result()
	{
		return result_flag;
	}

	// Callback methods (Returning from C code)
	private void backend_alert_msg_callback_handler(final String alert_msg)
	{
		JOptionPane.showMessageDialog(main_panel, alert_msg);
	}

	private void backend_fatal_alert_msg_callback_handler(final String alert_msg)
	{
		// Call to C function
		uninit_backend();

		// Notify alert message to user and then terminate the application
		JOptionPane.showMessageDialog(main_panel, alert_msg);
		System.exit(1);
	}
}



