import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.regex.*;
import javax.swing.border.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class EmU_Login extends JFrame implements ConstantVars
{
	private static final long serialVersionUID = -1113582265865921707L;

	// Declaration of the Native C functions
	private native void init_backend();
	private native void uninit_backend();
	private native boolean load_emergency_staff_authority_pub_key_main(String emergency_staff_auth_ip_addr);
	private native boolean user_login_main(String emergency_staff_auth_ip_addr, String username, String passwd);
	private native boolean admin_login_main(String emergency_staff_auth_ip_addr, String username, String passwd);

	// Variables
	private JPanel         main_panel                  	      = new JPanel();
	private ReentrantLock  working_lock                	      = new ReentrantLock();

	private JTextField     emergency_staff_auth_ip_addr_textfield = new JTextField(TEXTFIELD_LENGTH);
	private JTextField     username_textfield          	      = new JTextField(TEXTFIELD_LENGTH);
	private JPasswordField passwd_textfield            	      = new JPasswordField(TEXTFIELD_LENGTH);

	private JRadioButton[] user_type_radio_buttons     	      = new JRadioButton[2];
       	private ButtonGroup    user_type_group             	      = new ButtonGroup();
        private final String   login_as_user               	      = new String("User");
        private final String   login_as_admin              	      = new String("Admin");

	private JButton        login_button                	      = new JButton("Login");
	private JButton        forget_passwd_button        	      = new JButton("Forget a password?");

	// Return from backend
	private String         email_address;
	private String         authority_name;
	private String         mail_server_url;
	private String         authority_email_address;
	private String         authority_email_passwd;
	private String         ssl_cert_hash;

	public EmU_Login()
	{
		super("Emergency unit: Login Authentication");
		
		// Load JNI backend library
		System.loadLibrary("PHRapp_EmU_Login_JNI");

		working_lock.lock();

		// Call to C function
		init_backend();

		init_ui();
		setup_actions();

		working_lock.unlock();
	}

	private final void init_ui()
	{
		JLabel emergency_staff_auth_ip_addr_label = new JLabel("IP address: ", JLabel.RIGHT);
		JLabel username_label          		  = new JLabel("Username: ", JLabel.RIGHT);
		JLabel passwd_label            		  = new JLabel("Password: ", JLabel.RIGHT);

		JPanel upper_inner_panel = new JPanel(new SpringLayout());
		upper_inner_panel.add(emergency_staff_auth_ip_addr_label);
		upper_inner_panel.add(emergency_staff_auth_ip_addr_textfield);
		upper_inner_panel.add(username_label);
		upper_inner_panel.add(username_textfield);
		upper_inner_panel.add(passwd_label);
		upper_inner_panel.add(passwd_textfield);

		SpringUtilities.makeCompactGrid(upper_inner_panel, 3, 2, 5, 10, 10, 10);

		JPanel upper_outer_panel = new JPanel();
		upper_outer_panel.setLayout(new BoxLayout(upper_outer_panel, BoxLayout.X_AXIS));
		upper_outer_panel.setPreferredSize(new Dimension(310, 120));
		upper_outer_panel.setMaximumSize(new Dimension(310, 120));
		upper_outer_panel.setAlignmentX(0.5f);
		upper_outer_panel.add(upper_inner_panel);

		// User type
        	user_type_radio_buttons[0] = new JRadioButton(login_as_user);
        	user_type_radio_buttons[0].setActionCommand(login_as_user);

        	user_type_radio_buttons[1] = new JRadioButton(login_as_admin);
        	user_type_radio_buttons[1].setActionCommand(login_as_admin);

		user_type_radio_buttons[0].setSelected(true);
            	user_type_group.add(user_type_radio_buttons[0]);
		user_type_group.add(user_type_radio_buttons[1]);

		// User type panel
		JPanel user_type_inner_panel = new JPanel();
		user_type_inner_panel.setLayout(new BoxLayout(user_type_inner_panel, BoxLayout.Y_AXIS));
		user_type_inner_panel.setBorder(new EmptyBorder(new Insets(10, 20, 10, 20)));
		user_type_inner_panel.setPreferredSize(new Dimension(130, 70));
		user_type_inner_panel.setMaximumSize(new Dimension(130, 70));
		user_type_inner_panel.setAlignmentX(0.0f);
		user_type_inner_panel.add(user_type_radio_buttons[0]);
		user_type_inner_panel.add(user_type_radio_buttons[1]);

		JPanel user_type_outer_panel = new JPanel(new GridLayout(0, 1));
		user_type_outer_panel.setLayout(new BoxLayout(user_type_outer_panel, BoxLayout.Y_AXIS));
    		user_type_outer_panel.setBorder(BorderFactory.createTitledBorder("Login as:"));
		user_type_outer_panel.setAlignmentX(0.5f);
		user_type_outer_panel.add(user_type_inner_panel);

		// Login button
		JPanel login_button_panel = new JPanel();
		login_button_panel.setPreferredSize(new Dimension(260, 30));
		login_button_panel.setMaximumSize(new Dimension(260, 30));
		login_button_panel.setAlignmentX(0.5f);
		login_button_panel.add(login_button);

		// Forget passwd button
		JPanel forget_passwd_button_panel = new JPanel();
		forget_passwd_button_panel.setPreferredSize(new Dimension(260, 30));
		forget_passwd_button_panel.setMaximumSize(new Dimension(260, 30));
		forget_passwd_button_panel.setAlignmentX(0.5f);
		forget_passwd_button_panel.add(forget_passwd_button);

		// Main panel
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
		main_panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		main_panel.add(upper_outer_panel);
		main_panel.add(Box.createRigidArea(new Dimension(0, 10)));
		main_panel.add(user_type_outer_panel);
		main_panel.add(Box.createRigidArea(new Dimension(0, 10)));
		main_panel.add(login_button_panel);
		main_panel.add(Box.createRigidArea(new Dimension(0, 10)));
		main_panel.add(forget_passwd_button_panel);

		add(main_panel);

		setSize(360, 370);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void setup_actions()
	{
		// Set an event for close button
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
            		@Override
            		public void windowClosing(final WindowEvent e)
			{
				// We could not use tryLock() becuase the SwingUtilities the same thread even if
				// we call it manay times.Note that, the tryLock() could not detect the same thead
				if(!working_lock.isLocked())
				{
					working_lock.lock();
				}
				else
				{
					JOptionPane.showMessageDialog(main_panel, "Some task is working, please wait until the task is done");
					return;
				}

				// Call to C function
				uninit_backend();

				// Terminate the application
				System.exit(0);
            		}
        	});

		// Login button
		login_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				emergency_staff_auth_ip_addr_textfield.setEnabled(false);
				username_textfield.setEnabled(false);
				passwd_textfield.setEnabled(false);
				user_type_radio_buttons[0].setEnabled(false);
				user_type_radio_buttons[1].setEnabled(false);
				login_button.setEnabled(false);

				// We could not use tryLock() becuase the SwingUtilities the same thread even if
				// we call it manay times.Note that, the tryLock() could not detect the same thead
				if(!working_lock.isLocked())
				{
					working_lock.lock();
				}
				else
				{
					JOptionPane.showMessageDialog(main_panel, "Some task is working, please wait until the task is done");
					emergency_staff_auth_ip_addr_textfield.setEnabled(true);
					username_textfield.setEnabled(true);
					passwd_textfield.setEnabled(true);
					user_type_radio_buttons[0].setEnabled(true);
					user_type_radio_buttons[1].setEnabled(true);
					login_button.setEnabled(true);
					return;
				}		

				String emergency_staff_auth_ip_addr = emergency_staff_auth_ip_addr_textfield.getText();
				String username          	    = username_textfield.getText();
				String passwd            	    = new String(passwd_textfield.getPassword());

				// Validate Emergency Staff Authority's IP address, username and password
				if(!validate_inputs(emergency_staff_auth_ip_addr, username, passwd))
				{
					working_lock.unlock();

					emergency_staff_auth_ip_addr_textfield.setEnabled(true);
					username_textfield.setEnabled(true);
					passwd_textfield.setEnabled(true);
					user_type_radio_buttons[0].setEnabled(true);
					user_type_radio_buttons[1].setEnabled(true);
					login_button.setEnabled(true);
					return;
				}

				// Check for existence of a user authority's public key if it does not exist then load it
				if(!load_emergency_staff_authority_pub_key_main(emergency_staff_auth_ip_addr))  // Call to backend (C function)
				{
					working_lock.unlock();

					emergency_staff_auth_ip_addr_textfield.setEnabled(true);
					username_textfield.setEnabled(true);
					passwd_textfield.setEnabled(true);
					user_type_radio_buttons[0].setEnabled(true);
					user_type_radio_buttons[1].setEnabled(true);
					login_button.setEnabled(true);
					return;
				}

				String user_type = user_type_group.getSelection().getActionCommand();
				if(user_type.equals(login_as_user))
				{  
					// Call to backend (C function)
					if(user_login_main(emergency_staff_auth_ip_addr, username, passwd))
					{
						// Invisible Login frame and destroy it
						setVisible(false);
						dispose();

						// Call EmU_UserMain object
						EmU_UserMain user_main = new EmU_UserMain(username, passwd, email_address, 
							authority_name, emergency_staff_auth_ip_addr, ssl_cert_hash);

						user_main.setVisible(true);
					}
					else
					{
						emergency_staff_auth_ip_addr_textfield.requestFocus();
					}
				}
				else if(user_type.equals(login_as_admin))
				{  
					// Call to backend (C function)
					if(admin_login_main(emergency_staff_auth_ip_addr, username, passwd))
					{
						// Invisible Login frame and destroy it
						setVisible(false);
						dispose();

						// Call EmU_AdminMain object
						EmU_AdminMain admin_main = new EmU_AdminMain(username, passwd, email_address, authority_name, emergency_staff_auth_ip_addr, 
							mail_server_url, authority_email_address, authority_email_passwd, ssl_cert_hash);

						admin_main.setVisible(true);
					}
					else
					{
						emergency_staff_auth_ip_addr_textfield.requestFocus();
					}
				}

				working_lock.unlock();

				emergency_staff_auth_ip_addr_textfield.setEnabled(true);
				username_textfield.setEnabled(true);
				passwd_textfield.setEnabled(true);
				user_type_radio_buttons[0].setEnabled(true);
				user_type_radio_buttons[1].setEnabled(true);
				login_button.setEnabled(true);
			}
		});

		// Forget passwd button
		forget_passwd_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				forget_passwd_button.setEnabled(false);

				// We could not use tryLock() becuase the SwingUtilities the same thread even if
				// we call it manay times.Note that, the tryLock() could not detect the same thead
				if(!working_lock.isLocked())
				{
					working_lock.lock();
				}
				else
				{
					JOptionPane.showMessageDialog(main_panel, "Some task is working, please wait until the task is done");
					forget_passwd_button.setEnabled(true);
					return;
				}	

				// Call forget password object
				EmU_ForgetPassword forget_password_dialog = new EmU_ForgetPassword(main_panel);
				forget_password_dialog.setVisible(true);

				working_lock.unlock();
				forget_passwd_button.setEnabled(true);
			}
		});
	}

	private boolean validate_inputs(String emergency_staff_auth_ip_addr, String username, String passwd)
	{
		Pattern p;
		Matcher m;

		// Validate IP address
		p = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

		m = p.matcher(emergency_staff_auth_ip_addr);
		if(m.matches() == false)
		{
			JOptionPane.showMessageDialog(this, "Please input correct format for the IP address");
			return false;
		}

		// Validate username
		p = Pattern.compile("^[^-]*[a-zA-Z0-9_]+");

		m = p.matcher(username);
		if(m.matches() == false)
		{
			JOptionPane.showMessageDialog(this, "Please input correct format for the username");
			return false;
		}
		
		// Validate passwd
		if(!(passwd.length() >= PASSWD_LENGTH_LOWER_BOUND && passwd.length() <= PASSWD_LENGTH_UPPER_BOUND))
		{
			JOptionPane.showMessageDialog(this, "Please input the password's length between " + 
				PASSWD_LENGTH_LOWER_BOUND + " and " + PASSWD_LENGTH_UPPER_BOUND + " characters");

			return false;
		}

		p = Pattern.compile("^[^-]*[a-zA-Z0-9\\_&$%#@*+-/]+");
		m = p.matcher(passwd);
		if(m.matches() == false)
		{
			JOptionPane.showMessageDialog(this, "Please input correct format for the password");
			return false;
		}

		return true;
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

	private synchronized void basic_info_ret_callback_handler(String email_address, String authority_name)
	{
		this.email_address            = email_address;
		this.authority_name           = authority_name;
	}

	// This method is called only if the user is an admin
	private synchronized void mail_server_configuration_ret_callback_handler(String mail_server_url, String authority_email_address, String authority_email_passwd)
	{
		this.mail_server_url         = mail_server_url;
		this.authority_email_address = authority_email_address;
		this.authority_email_passwd  = authority_email_passwd;
	}

	private synchronized void ssl_cert_hash_ret_callback_handler(String ssl_cert_hash)
	{
		this.ssl_cert_hash = ssl_cert_hash;
	}

	// Main method
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
            		public void run()
			{
				EmU_Login login_gui = new EmU_Login();
				login_gui.setVisible(true);
            		}
        	});
	}
}



