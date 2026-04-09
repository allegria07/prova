package plane;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class TravelAgency extends JFrame {

	// DATI E GESTORI
	private CaricaDati dati;
	private GestoreUtenti gestoreUtenti;
	private GestoreRistoranti gestoreRistoranti;
	private GestoreItinerari gestoreItinerari;
	private Utente utenteCorrente;

	// PANNELLI DELL'APPLICAZIONE
	private JPanel pannelloLogin;
	private JPanel pannelloRegistrazione;
	private JPanel pannelloRicerca;
	private JPanel pannelloDettaglio;
	private JPanel pannelloItinerari;
	private JPanel header;

	// COMPONENTI HEADER
	private JLabel labelUtente;
	private JButton btnLogout;
	private JButton btnMieiItinerari;

	// FORMATO DATA
	static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	// COSTRUTTORE
	public TravelAgency() {
		setTitle("Travel Agency");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000, 800);
		setMinimumSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// CARICAMENTO DATI
		dati = new CaricaDati();
		gestoreUtenti = new GestoreUtenti();
		gestoreRistoranti = new GestoreRistoranti();
		gestoreItinerari = GestoreItinerari.caricaDaFile(dati, gestoreRistoranti);

		// CREAZIONE HEADER E PANNELLI
		creaHeader();
		pannelloLogin = creaLogin();
		pannelloRegistrazione = creaRegistrazione();
		pannelloRicerca = creaRicerca();
		pannelloDettaglio = new JPanel(new BorderLayout());
		pannelloItinerari = creaItinerari();

		// MOSTRA LOGIN ALL'AVVIO
		mostraPannello(pannelloLogin);
		setVisible(true);
	}

	// CREA IL PANNELLO HEADER IN CIMA ALLA FINESTRA
	private void creaHeader() {
		header = new JPanel(new BorderLayout());
		header.setBackground(new Color(240, 240, 240));
		header.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		// ETICHETTA STATO UTENTE (sinistra)
		labelUtente = new JLabel("Non autenticato");
		labelUtente.setFont(new Font("Arial", Font.PLAIN, 12));
		header.add(labelUtente, BorderLayout.WEST);

		// PULSANTI (destra)
		JPanel azioni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		azioni.setOpaque(false);

		btnMieiItinerari = new JButton("I miei Itinerari");
		btnMieiItinerari.setVisible(false);
		btnMieiItinerari.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraItinerari();
			}
		});

		btnLogout = new JButton("Logout");
		btnLogout.setVisible(false);
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logout();
			}
		});

		azioni.add(btnMieiItinerari);
		azioni.add(btnLogout);
		header.add(azioni, BorderLayout.EAST);
		add(header, BorderLayout.NORTH);
	}

	// SOSTITUISCE IL PANNELLO CENTRALE CON QUELLO PASSATO
	private void mostraPannello(JPanel p) {
		getContentPane().removeAll();
		getContentPane().add(header, BorderLayout.NORTH);
		getContentPane().add(p, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	// AGGIORNA L'HEADER IN BASE ALL'UTENTE LOGGATO
	private void aggiornaHeader() {
		if (utenteCorrente != null) {
			labelUtente.setText("Utente: " + utenteCorrente.username + " (" + utenteCorrente.email + ")");
			btnMieiItinerari.setVisible(true);
			btnLogout.setVisible(true);
		} else {
			labelUtente.setText("Non autenticato");
			btnMieiItinerari.setVisible(false);
			btnLogout.setVisible(false);
		}
		revalidate();
		repaint();
	}

	// ESEGUE IL LOGOUT E TORNA AL LOGIN
	private void logout() {
		utenteCorrente = null;
		aggiornaHeader();
		mostraPannello(pannelloLogin);
	}

	// MOSTRA LA SCHERMATA DI RICERCA
	private void mostraRicerca() {
		mostraPannello(pannelloRicerca);
	}

	// MOSTRA LA SCHERMATA ITINERARI (ricreata ogni volta per aggiornarsi)
	private void mostraItinerari() {
		pannelloItinerari = creaItinerari();
		mostraPannello(pannelloItinerari);
	}

	// MOSTRA IL DETTAGLIO DI UNA CITTÀ
	private void mostraDettaglioCitta(Citta citta) {
		pannelloDettaglio = creaDettaglioCitta(citta);
		mostraPannello(pannelloDettaglio);
	}

	// PANNELLO LOGIN

	private JPanel creaLogin() {
		JPanel sfondo = new JPanel(new GridBagLayout());
		sfondo.setBackground(Color.WHITE);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Color.WHITE);
		form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		form.setPreferredSize(new Dimension(350, 280));

		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(5, 5, 5, 5);

		// TITOLO
		JLabel titolo = new JLabel("Accedi");
		titolo.setFont(new Font("Arial", Font.BOLD, 24));
		g.gridx = 0;
		g.gridy = 0;
		g.gridwidth = 2;
		g.anchor = GridBagConstraints.CENTER;
		g.insets = new Insets(5, 5, 15, 5);
		form.add(titolo, g);

		g.gridwidth = 1;
		g.anchor = GridBagConstraints.LINE_START;
		g.insets = new Insets(5, 5, 5, 5);

		// CAMPO USERNAME
		form.add(new JLabel("Username o Email:"), gbc(g, 0, 1));
		final JTextField campoUsername = new JTextField(20);
		form.add(campoUsername, gbc(g, 0, 2));

		// CAMPO PASSWORD
		form.add(new JLabel("Password:"), gbc(g, 0, 3));
		final JPasswordField campoPassword = new JPasswordField(20);
		form.add(campoPassword, gbc(g, 0, 4));

		// ETICHETTA ERRORE
		final JLabel errore = new JLabel(" ");
		errore.setForeground(Color.RED);
		form.add(errore, gbc(g, 0, 5));

		// PULSANTI
		JPanel pulsanti = new JPanel(new FlowLayout());
		pulsanti.setBackground(Color.WHITE);
		JButton btnEntra = new JButton("Accedi");
		JButton btnRegistrati = new JButton("Registrati");
		pulsanti.add(btnEntra);
		pulsanti.add(btnRegistrati);
		g.gridx = 0;
		g.gridy = 6;
		g.anchor = GridBagConstraints.CENTER;
		form.add(pulsanti, g);

		sfondo.add(form);

		// AZIONE PULSANTE ACCEDI
		btnEntra.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String u = campoUsername.getText().trim();
				String p = new String(campoPassword.getPassword());

				if (u.isEmpty() || p.isEmpty()) {
					errore.setText("Campi obbligatori");
					return;
				}

				Utente loggato = gestoreUtenti.login(u, p);
				if (loggato != null) {
					utenteCorrente = loggato;
					aggiornaHeader();
					mostraPannello(pannelloRicerca);
				} else {
					errore.setText("Credenziali errate");
				}
			}
		});

		// AZIONE PULSANTE REGISTRATI
		btnRegistrati.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraPannello(pannelloRegistrazione);
			}
		});

		return sfondo;
	}

	// PANNELLO REGISTRAZIONE

	private JPanel creaRegistrazione() {
		JPanel sfondo = new JPanel(new GridBagLayout());
		sfondo.setBackground(Color.WHITE);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(Color.WHITE);
		form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		form.setPreferredSize(new Dimension(400, 380));

		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(5, 5, 5, 5);

		// TITOLO
		JLabel titolo = new JLabel("Registrazione");
		titolo.setFont(new Font("Arial", Font.BOLD, 24));
		g.gridx = 0;
		g.gridy = 0;
		g.gridwidth = 2;
		g.anchor = GridBagConstraints.CENTER;
		g.insets = new Insets(5, 5, 15, 5);
		form.add(titolo, g);

		g.gridwidth = 1;
		g.anchor = GridBagConstraints.LINE_START;
		g.insets = new Insets(5, 5, 5, 5);

		// CAMPI
		form.add(new JLabel("Username:"), gbc(g, 0, 1));
		final JTextField campoUser = new JTextField(20);
		form.add(campoUser, gbc(g, 0, 2));

		form.add(new JLabel("Email:"), gbc(g, 0, 3));
		final JTextField campoEmail = new JTextField(20);
		form.add(campoEmail, gbc(g, 0, 4));

		form.add(new JLabel("Password:"), gbc(g, 0, 5));
		final JPasswordField campoPass = new JPasswordField(20);
		form.add(campoPass, gbc(g, 0, 6));

		form.add(new JLabel("Conferma:"), gbc(g, 0, 7));
		final JPasswordField campoPass2 = new JPasswordField(20);
		form.add(campoPass2, gbc(g, 0, 8));

		// ETICHETTA ERRORE
		final JLabel errore = new JLabel(" ");
		errore.setForeground(Color.RED);
		form.add(errore, gbc(g, 0, 9));

		// PULSANTI
		JPanel pulsanti = new JPanel(new FlowLayout());
		pulsanti.setBackground(Color.WHITE);
		JButton btnRegist = new JButton("Registrati");
		JButton btnTorna = new JButton("Torna al Login");
		pulsanti.add(btnRegist);
		pulsanti.add(btnTorna);
		g.gridx = 0;
		g.gridy = 10;
		g.anchor = GridBagConstraints.CENTER;
		form.add(pulsanti, g);

		sfondo.add(form);

		// AZIONE REGISTRATI
		btnRegist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String u = campoUser.getText().trim();
				String em = campoEmail.getText().trim();
				String p1 = new String(campoPass.getPassword());
				String p2 = new String(campoPass2.getPassword());

				if (u.isEmpty() || em.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
					errore.setText("Tutti i campi obbligatori");
					return;
				}
				if (!p1.equals(p2)) {
					errore.setText("Le password non combaciano");
					return;
				}
				if (!gestoreUtenti.emailValida(em)) {
					errore.setText("Email non valida");
					return;
				}
				if (gestoreUtenti.registra(u, em, p1)) {
					JOptionPane.showMessageDialog(null, "Registrazione completata!", "OK",
							JOptionPane.INFORMATION_MESSAGE);
					mostraPannello(pannelloLogin);
				} else {
					errore.setText("Username o email già in uso");
				}
			}
		});

		// AZIONE TORNA AL LOGIN
		btnTorna.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraPannello(pannelloLogin);
			}
		});

		return sfondo;
	}

	// PANNELLO RICERCA

	private JPanel creaRicerca() {
		JPanel contenitore = new JPanel(new BorderLayout());
		contenitore.setBorder(new EmptyBorder(20, 20, 20, 20));

		// ZONA SUPERIORE CON TITOLO E BARRA DI RICERCA
		JPanel zonaRicerca = new JPanel();
		zonaRicerca.setLayout(new BoxLayout(zonaRicerca, BoxLayout.Y_AXIS));
		zonaRicerca.setBorder(new EmptyBorder(80, 0, 40, 0));

		JLabel titolo = new JLabel("Travel Agency", SwingConstants.CENTER);
		titolo.setFont(new Font("Arial", Font.BOLD, 36));
		titolo.setForeground(new Color(66, 133, 244));
		titolo.setAlignmentX(CENTER_ALIGNMENT);
		zonaRicerca.add(titolo);

		JLabel sottotitolo = new JLabel("Trova la tua prossima destinazione", SwingConstants.CENTER);
		sottotitolo.setFont(new Font("Arial", Font.PLAIN, 16));
		sottotitolo.setAlignmentX(CENTER_ALIGNMENT);
		zonaRicerca.add(sottotitolo);
		zonaRicerca.add(Box.createVerticalStrut(30));

		// BARRA DI RICERCA
		JPanel barraRicerca = new JPanel();
		barraRicerca.setLayout(new BoxLayout(barraRicerca, BoxLayout.X_AXIS));
		barraRicerca.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
						BorderFactory.createEmptyBorder(10, 15, 10, 15)));
		barraRicerca.setBackground(Color.WHITE);
		barraRicerca.setMaximumSize(new Dimension(600, 50));
		barraRicerca.setAlignmentX(CENTER_ALIGNMENT);

		final JTextField campoRicerca = new JTextField();
		campoRicerca.setBorder(BorderFactory.createEmptyBorder());
		campoRicerca.setFont(new Font("Arial", Font.PLAIN, 16));
		barraRicerca.add(campoRicerca);
		barraRicerca.add(Box.createHorizontalStrut(10));

		JButton btnCerca = new JButton("Cerca");
		btnCerca.setFont(new Font("Arial", Font.BOLD, 14));
		barraRicerca.add(btnCerca);
		zonaRicerca.add(barraRicerca);
		contenitore.add(zonaRicerca, BorderLayout.NORTH);

		// PANNELLO RISULTATI
		final JPanel risultati = new JPanel();
		risultati.setLayout(new BoxLayout(risultati, BoxLayout.Y_AXIS));
		JScrollPane scroll = new JScrollPane(risultati);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		contenitore.add(scroll, BorderLayout.CENTER);

		// AZIONE DI RICERCA (condivisa tra pulsante e tasto invio)
		ActionListener azioneCerca = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String q = campoRicerca.getText().trim();
				risultati.removeAll();

				List<Citta> trovate;
				if (q.isEmpty()) {
					trovate = dati.getTutte();
				} else {
					trovate = dati.cerca(q);
				}

				for (int i = 0; i < trovate.size(); i++) {
					final Citta c = trovate.get(i);
					JButton btnCitta = new JButton(c.nome);
					btnCitta.setFont(new Font("Arial", Font.BOLD, 16));
					btnCitta.setAlignmentX(LEFT_ALIGNMENT);
					btnCitta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
					btnCitta.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ev) {
							mostraDettaglioCitta(c);
						}
					});
					risultati.add(btnCitta);
					risultati.add(Box.createVerticalStrut(5));
				}

				risultati.revalidate();
				risultati.repaint();
			}
		};

		btnCerca.addActionListener(azioneCerca);
		campoRicerca.addActionListener(azioneCerca);

		return contenitore;
	}

	// PANNELLO DETTAGLIO CITTÀ

	private JPanel creaDettaglioCitta(final Citta citta) {
		JPanel pannello = new JPanel(new BorderLayout(0, 20));
		pannello.setBorder(new EmptyBorder(20, 20, 20, 20));

		// PULSANTE TORNA INDIETRO
		JButton btnTorna = new JButton("← Torna alla ricerca");
		btnTorna.setFont(new Font("Arial", Font.BOLD, 14));
		btnTorna.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraPannello(pannelloRicerca);
			}
		});
		pannello.add(btnTorna, BorderLayout.NORTH);

		// CONTENUTO DELLA PAGINA
		JPanel contenuto = new JPanel();
		contenuto.setLayout(new BoxLayout(contenuto, BoxLayout.Y_AXIS));

		JLabel nomeCitta = new JLabel(citta.nome);
		nomeCitta.setFont(new Font("Arial", Font.BOLD, 32));
		nomeCitta.setAlignmentX(LEFT_ALIGNMENT);
		contenuto.add(nomeCitta);
		contenuto.add(Box.createVerticalStrut(20));

		// GALLERIA IMMAGINI (max 3)
		if (!citta.immagini.isEmpty()) {
			JPanel galleria = new JPanel(new FlowLayout(FlowLayout.LEFT));
			galleria.setAlignmentX(LEFT_ALIGNMENT);

			int numImmagini = citta.immagini.size();
			if (numImmagini > 3) {
				numImmagini = 3;
			}

			for (int i = 0; i < numImmagini; i++) {
				final String url = citta.immagini.get(i);
				final JLabel imgLabel = new JLabel("Caricamento...");
				imgLabel.setPreferredSize(new Dimension(300, 200));
				imgLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
				galleria.add(imgLabel);

				// CARICAMENTO ASINCRONO DELL'IMMAGINE
				new SwingWorker<ImageIcon, Void>() {
					protected ImageIcon doInBackground() {
						return CaricoImmagini.carica(url, 300, 200);
					}

					protected void done() {
						try {
							ImageIcon icon = get();
							if (icon != null) {
								imgLabel.setIcon(icon);
								imgLabel.setText(null);
							} else {
								imgLabel.setText("N/D");
							}
						} catch (Exception ex) {
							imgLabel.setText("N/D");
						}
					}
				}.execute();
			}

			TitledBorder bordo = BorderFactory.createTitledBorder("Galleria");
			bordo.setTitleFont(new Font("Arial", Font.BOLD, 16));
			galleria.setBorder(bordo);
			contenuto.add(galleria);
			contenuto.add(Box.createVerticalStrut(15));
		}

		// SEZIONI INFO
		contenuto.add(creaSezione("Hotel", citta.hotel));
		contenuto.add(Box.createVerticalStrut(15));
		contenuto.add(creaSezione("Cibi tipici", citta.cibi));
		contenuto.add(Box.createVerticalStrut(15));
		contenuto.add(creaSezione("Attrazioni", citta.attrazioni));
		contenuto.add(Box.createVerticalStrut(20));

		// PULSANTE PRENOTA (solo se loggato)
		if (utenteCorrente != null) {
			JButton btnItinerario = new JButton("Crea itinerario per " + citta.nome);
			btnItinerario.setFont(new Font("Arial", Font.BOLD, 16));
			btnItinerario.setAlignmentX(LEFT_ALIGNMENT);
			btnItinerario.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mostraDialogCreaItinerario(citta);
				}
			});
			contenuto.add(btnItinerario);
		}

		JScrollPane scroll = new JScrollPane(contenuto);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		pannello.add(scroll, BorderLayout.CENTER);

		return pannello;
	}

	// CREA UNA SEZIONE (hotel, cibi, attrazioni) CON TITOLO E LISTA
	private JPanel creaSezione(String titolo, List<String> elementi) {
		JPanel sezione = new JPanel();
		sezione.setLayout(new BoxLayout(sezione, BoxLayout.Y_AXIS));
		sezione.setAlignmentX(LEFT_ALIGNMENT);

		TitledBorder bordo = BorderFactory.createTitledBorder(titolo);
		bordo.setTitleFont(new Font("Arial", Font.BOLD, 16));
		sezione.setBorder(bordo);

		for (int i = 0; i < elementi.size(); i++) {
			JLabel l = new JLabel("• " + elementi.get(i));
			l.setFont(new Font("Arial", Font.PLAIN, 14));
			sezione.add(l);
		}

		return sezione;
	}

	// DIALOG PER CREARE UN ITINERARIO PER UNA CITTÀ SPECIFICA
	private void mostraDialogCreaItinerario(final Citta citta) {
		JPanel dialog = new JPanel(new GridBagLayout());
		dialog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(5, 5, 5, 5);

		dialog.add(new JLabel("Nome:"), gbc(g, 0, 0));
		final JTextField campoNome = new JTextField("Viaggio a " + citta.nome, 20);
		dialog.add(campoNome, gbc(g, 1, 0));

		dialog.add(new JLabel("Inizio (gg/mm/aaaa):"), gbc(g, 0, 1));
		final JTextField campoInizio = new JTextField("01/06/2025", 10);
		dialog.add(campoInizio, gbc(g, 1, 1));

		dialog.add(new JLabel("Fine (gg/mm/aaaa):"), gbc(g, 0, 2));
		final JTextField campoFine = new JTextField("07/06/2025", 10);
		dialog.add(campoFine, gbc(g, 1, 2));

		dialog.add(new JLabel("Persone:"), gbc(g, 0, 3));
		final JSpinner spinPersone = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
		dialog.add(spinPersone, gbc(g, 1, 3));

		int scelta = JOptionPane.showConfirmDialog(this, dialog, "Crea Itinerario", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (scelta == JOptionPane.OK_OPTION) {
			try {
				LocalDate inizio = LocalDate.parse(campoInizio.getText(), FMT);
				LocalDate fine = LocalDate.parse(campoFine.getText(), FMT);

				if (fine.isBefore(inizio)) {
					JOptionPane.showMessageDialog(this, "Errore date!");
					return;
				}

				Itinerario it = gestoreItinerari.creaConCitta(utenteCorrente, campoNome.getText().trim(), inizio, fine,
						(Integer) spinPersone.getValue(), citta);

				JOptionPane.showMessageDialog(this, "Itinerario creato!\n" + it);
				mostraItinerari();

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Dati invalidi.");
			}
		}
	}

	// PANNELLO ITINERARI

	private JPanel creaItinerari() {
		JPanel pannello = new JPanel(new BorderLayout());
		pannello.setBorder(new EmptyBorder(20, 20, 20, 20));

		JLabel titolo = new JLabel("I miei Itinerari");
		titolo.setFont(new Font("Arial", Font.BOLD, 28));
		pannello.add(titolo, BorderLayout.NORTH);

		// LISTA DEGLI ITINERARI
		JPanel lista = new JPanel();
		lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));

		if (utenteCorrente != null) {
			List<Itinerario> itinerari = gestoreItinerari.getItinerari(utenteCorrente);

			if (itinerari.isEmpty()) {
				lista.add(new JLabel("Nessun itinerario."));
			} else {
				for (int i = 0; i < itinerari.size(); i++) {
					lista.add(creaCardItinerario(itinerari.get(i)));
					lista.add(Box.createVerticalStrut(10));
				}
			}
		}

		JScrollPane scroll = new JScrollPane(lista);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		pannello.add(scroll, BorderLayout.CENTER);

		// PULSANTE CREA RANDOM
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton btnNuovo = new JButton("+ Crea itinerario casuale");
		btnNuovo.setFont(new Font("Arial", Font.BOLD, 14));
		btnNuovo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraDialogCreaRandom();
			}
		});
		footer.add(btnNuovo);
		pannello.add(footer, BorderLayout.SOUTH);

		return pannello;
	}

	// CREA UNA CARD PER UN SINGOLO ITINERARIO
	private JPanel creaCardItinerario(final Itinerario it) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(new Color(245, 245, 250));
		card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)),
				BorderFactory.createEmptyBorder(10, 15, 10, 15)));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		card.setAlignmentX(LEFT_ALIGNMENT);

		// INFO ITINERARIO
		JPanel info = new JPanel();
		info.setOpaque(false);
		info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

		JLabel lNome = new JLabel(it.nome);
		lNome.setFont(new Font("Arial", Font.BOLD, 16));

		JLabel lDate = new JLabel("Dal " + it.dataInizio.format(FMT) + " al " + it.dataFine.format(FMT));
		JLabel lPers = new JLabel("Persone: " + it.persone);

		JLabel lPrezzo = new JLabel(String.format("Prezzo: € %.2f", it.prezzoTotale));
		lPrezzo.setForeground(new Color(0, 128, 0));

		info.add(lNome);
		info.add(lDate);
		info.add(lPers);
		info.add(lPrezzo);
		card.add(info, BorderLayout.CENTER);

		// PULSANTI DELLA CARD
		JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pulsanti.setOpaque(false);

		JButton btnDet = new JButton("Dettagli");
		JButton btnElim = new JButton("Elimina");

		btnDet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraDettagliItinerario(it);
			}
		});

		btnElim.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int risposta = JOptionPane.showConfirmDialog(null, "Eliminare?", "Conferma", JOptionPane.YES_NO_OPTION);
				if (risposta == JOptionPane.YES_OPTION) {
					gestoreItinerari.elimina(it);
					mostraItinerari();
				}
			}
		});

		pulsanti.add(btnDet);
		pulsanti.add(btnElim);
		card.add(pulsanti, BorderLayout.EAST);

		return card;
	}

	// MOSTRA UN DIALOG CON LE TAPPE DELL'ITINERARIO
	private void mostraDettagliItinerario(Itinerario it) {
		JPanel det = new JPanel();
		det.setLayout(new BoxLayout(det, BoxLayout.Y_AXIS));
		det.setBackground(Color.WHITE);

		JLabel titTappe = new JLabel("Tappe:");
		titTappe.setFont(new Font("Arial", Font.BOLD, 16));
		det.add(titTappe);
		det.add(Box.createVerticalStrut(8));

		for (int i = 0; i < it.tappe.size(); i++) {
			Tappa t = it.tappe.get(i);

			JPanel card = new JPanel();
			card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
			card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			card.setAlignmentX(LEFT_ALIGNMENT);

			card.add(new JLabel("Giorno " + (i + 1) + " - " + t.citta.nome));
			card.add(new JLabel("Hotel: " + t.hotel));
			card.add(new JLabel("Attrazione: " + t.attrazione));

			det.add(card);
			det.add(Box.createVerticalStrut(8));
		}

		JScrollPane sc = new JScrollPane(det);
		sc.setPreferredSize(new Dimension(500, 300));
		JOptionPane.showMessageDialog(this, sc, "Dettagli Itinerario", JOptionPane.PLAIN_MESSAGE);
	}

	// DIALOG PER CREARE UN ITINERARIO CASUALE
	private void mostraDialogCreaRandom() {
		JPanel dialog = new JPanel(new GridBagLayout());
		GridBagConstraints g = new GridBagConstraints();
		g.fill = GridBagConstraints.HORIZONTAL;
		g.insets = new Insets(5, 5, 5, 5);

		dialog.add(new JLabel("Nome:"), gbc(g, 0, 0));
		final JTextField campoNome = new JTextField("Il mio viaggio", 20);
		dialog.add(campoNome, gbc(g, 1, 0));

		dialog.add(new JLabel("Inizio:"), gbc(g, 0, 1));
		final JTextField cInizio = new JTextField("08/06/2026", 10);
		dialog.add(cInizio, gbc(g, 1, 1));

		dialog.add(new JLabel("Fine:"), gbc(g, 0, 2));
		final JTextField cFine = new JTextField("14/06/2025", 10);
		dialog.add(cFine, gbc(g, 1, 2));

		dialog.add(new JLabel("Persone:"), gbc(g, 0, 3));
		final JSpinner sPer = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
		dialog.add(sPer, gbc(g, 1, 3));

		dialog.add(new JLabel("Città:"), gbc(g, 0, 4));
		final JSpinner sCit = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
		dialog.add(sCit, gbc(g, 1, 4));

		int scelta = JOptionPane.showConfirmDialog(this, dialog, "Crea Random", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (scelta == JOptionPane.OK_OPTION) {
			try {
				LocalDate ini = LocalDate.parse(cInizio.getText(), FMT);
				LocalDate fin = LocalDate.parse(cFine.getText(), FMT);

				if (fin.isBefore(ini)) {
					JOptionPane.showMessageDialog(this, "Errore date");
					return;
				}

				gestoreItinerari.creaRandom(utenteCorrente, campoNome.getText(), ini, fin, (Integer) sPer.getValue(),
						(Integer) sCit.getValue());

				mostraItinerari();

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, "Dati invalidi");
			}
		}
	}

	// UTILITY PER IMPOSTARE GRIDX E GRIDY IN UN SOLO COMANDO
	private GridBagConstraints gbc(GridBagConstraints g, int x, int y) {
		g.gridx = x;
		g.gridy = y;
		return g;
	}

	// METODO MAIN
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new TravelAgency();
			}
		});
	}
}

//  MODELLI

class Utente implements Serializable {
	String username;
	String email;
	String password;

	Utente(String u, String e, String p) {
		username = u;
		email = e;
		password = p;
	}

	public String toString() {
		return username + "," + email + "," + password;
	}
}

class Citta implements Serializable {
	String nome;
	List<String> immagini = new ArrayList<String>();
	List<String> hotel = new ArrayList<String>();
	List<String> cibi = new ArrayList<String>();
	List<String> attrazioni = new ArrayList<String>();

	Citta(String nome) {
		this.nome = nome;
	}

	void addImmagine(String u) {
		immagini.add(u);
	}

	void addHotel(String h) {
		hotel.add(h);
	}

	void addCibo(String c) {
		cibi.add(c);
	}

	void addAttrazione(String a) {
		attrazioni.add(a);
	}

	public String toString() {
		return nome;
	}
}

class Ristorante implements Serializable {
	String nome, citta, indirizzo, cucina, prezzi, descrizione;
	double voto;

	Ristorante(String n, String c, String i, String cu, double v, String p, String d) {
		nome = n;
		citta = c;
		indirizzo = i;
		cucina = cu;
		voto = v;
		prezzi = p;
		descrizione = d;
	}
}

class Tappa implements Serializable {
	Citta citta;
	String hotel;
	String attrazione;
	Ristorante ristorante;
	LocalDate data;

	Tappa(Citta c, String h, Ristorante r, String a, LocalDate d) {
		citta = c;
		hotel = h;
		ristorante = r;
		attrazione = a;
		data = d;
	}
}

class Itinerario implements Serializable {
	String id = UUID.randomUUID().toString();
	String nome;
	Utente utente;
	LocalDate dataInizio;
	LocalDate dataFine;
	int persone;
	double prezzoTotale;
	List<Tappa> tappe = new ArrayList<Tappa>();

	Itinerario(String n, Utente u, LocalDate i, LocalDate f, int p) {
		nome = n;
		utente = u;
		dataInizio = i;
		dataFine = f;
		persone = p;
	}

	void addTappa(Tappa t) {
		tappe.add(t);
	}

	int getDurata() {
		return (int) (dataFine.toEpochDay() - dataInizio.toEpochDay() + 1);
	}

	void calcolaPrezzo() {
		prezzoTotale = getDurata() * persone * 100.0;
	}

	public String toString() {
		return nome + " (" + getDurata() + " giorni)";
	}
}

//  GESTORI

class GestoreUtenti {
	private List<Utente> utenti = new ArrayList<Utente>();
	private static final Pattern EMAIL_OK = Pattern.compile(".*@.*");

	// COSTRUTTORE: CARICA UTENTI DAL FILE
	GestoreUtenti() {
		File f = new File("users.txt");
		if (!f.exists()) {
			return;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String r;
			while ((r = br.readLine()) != null) {
				String[] p = r.split(",");
				if (p.length == 3) {
					utenti.add(new Utente(p[0], p[1], p[2]));
				}
			}
		} catch (Exception e) {
			System.err.println("Errore caricamento utenti: " + e.getMessage());
		}
	}

	// REGISTRA UN NUOVO UTENTE
	boolean registra(String u, String e, String p) {
		if (!EMAIL_OK.matcher(e).matches()) {
			return false;
		}
		for (int i = 0; i < utenti.size(); i++) {
			if (utenti.get(i).username.equals(u) || utenti.get(i).email.equals(e)) {
				return false;
			}
		}
		utenti.add(new Utente(u, e, p));
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("users.txt"))) {
			for (int i = 0; i < utenti.size(); i++) {
				bw.write(utenti.get(i).toString() + "\n");
			}
		} catch (Exception ex) {
			System.err.println("Errore salvataggio utenti: " + ex.getMessage());
		}
		return true;
	}

	// ESEGUE IL LOGIN
	Utente login(String u, String p) {
		for (int i = 0; i < utenti.size(); i++) {
			Utente ut = utenti.get(i);
			if ((ut.username.equals(u) || ut.email.equals(u)) && ut.password.equals(p)) {
				return ut;
			}
		}
		return null;
	}

	// VERIFICA CHE L'EMAIL CONTENGA @
	boolean emailValida(String e) {
		return EMAIL_OK.matcher(e).matches();
	}
}

class FileLoader {
	// CERCA IL FILE IN PIU' POSIZIONI PER EVITARE NULLPOINTEREXCEPTION
	static InputStream getStream(String nome) {
		InputStream is = FileLoader.class.getResourceAsStream("/" + nome);
		if (is != null) {
			return is;
		}
		try {
			File f = new File(nome);
			if (f.exists()) {
				return new FileInputStream(f);
			}
			f = new File("src/" + nome);
			if (f.exists()) {
				return new FileInputStream(f);
			}
		} catch (Exception e) {
			System.err.println("FileLoader: " + e.getMessage());
		}
		return null;
	}
}

class CaricaDati implements Serializable {
	private Map<String, Citta> mappa = new HashMap<String, Citta>();

	// COSTRUTTORE: CARICA IMMAGINI E INFO DELLE CITTÀ
	CaricaDati() {

		// CARICA IMMAGINI CITTÀ
		InputStream is = FileLoader.getStream("city_images.txt");
		if (is != null) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String r;
				while ((r = br.readLine()) != null) {
					String[] p = r.split(",");
					if (p.length > 0) {
						Citta c = new Citta(p[0]);
						for (int i = 1; i < p.length; i++) {
							c.addImmagine(p[i]);
						}
						mappa.put(p[0].toLowerCase(), c);
					}
				}
			} catch (Exception e) {
				System.err.println("Errore city_images: " + e.getMessage());
			}
		}

		// CARICA INFO CITTÀ (hotel, cibo, attrazioni)
		is = FileLoader.getStream("city_info.txt");
		if (is != null) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String r;
				while ((r = br.readLine()) != null) {
					String[] p = r.split("\\|");
					if (p.length < 4) {
						continue;
					}
					Citta c = mappa.get(p[0].toLowerCase());
					if (c == null) {
						continue;
					}
					for (String h : p[1].split(";"))
						c.addHotel(h);
					for (String f : p[2].split(";"))
						c.addCibo(f);
					for (String a : p[3].split(";"))
						c.addAttrazione(a);
				}
			} catch (Exception e) {
				System.err.println("Errore city_info: " + e.getMessage());
			}
		}
	}

	// CERCA CITTÀ PER NOME (parziale)
	List<Citta> cerca(String q) {
		List<Citta> res = new ArrayList<Citta>();
		String lower = q.toLowerCase();
		for (Citta c : mappa.values()) {
			if (c.nome.toLowerCase().contains(lower)) {
				res.add(c);
			}
		}
		return res;
	}

	// RESTITUISCE TUTTE LE CITTÀ
	List<Citta> getTutte() {
		return new ArrayList<Citta>(mappa.values());
	}
}

class GestoreRistoranti implements Serializable {
	private Map<String, List<Ristorante>> mappa = new HashMap<String, List<Ristorante>>();

	// COSTRUTTORE: CARICA RISTORANTI DAL FILE
	GestoreRistoranti() {
		InputStream is = FileLoader.getStream("ristoranti.txt");
		if (is != null) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String r;
				while ((r = br.readLine()) != null) {
					String[] p = r.split("\\|");
					if (p.length < 7) {
						continue;
					}
					double voto = 0.0;
					try {
						voto = Double.parseDouble(p[4].replace(",", "."));
					} catch (Exception e) {
						voto = 0.0;
					}
					String key = p[0].toLowerCase();
					if (!mappa.containsKey(key)) {
						mappa.put(key, new ArrayList<Ristorante>());
					}
					mappa.get(key).add(new Ristorante(p[1], p[0], p[2], p[3], voto, p[5], p[6]));
				}
			} catch (Exception e) {
				System.err.println("Errore ristoranti: " + e.getMessage());
			}
		}
	}

	// RESTITUISCE UN RISTORANTE CASUALE PER UNA CITTÀ
	Ristorante getRistoranteCasuale(String c) {
		List<Ristorante> l = mappa.get(c.toLowerCase());
		if (l == null || l.isEmpty()) {
			return null;
		}
		return l.get(new Random().nextInt(l.size()));
	}
}

class GestoreItinerari implements Serializable {
	private Map<String, List<Itinerario>> mappa = new HashMap<String, List<Itinerario>>();
	private transient CaricaDati dati;
	private transient GestoreRistoranti ristoranti;
	private transient Random rand;

	// COSTRUTTORE
	GestoreItinerari(CaricaDati d, GestoreRistoranti r) {
		dati = d;
		ristoranti = r;
		rand = new Random();
	}

	// CARICA DA FILE O CREA NUOVO
	static GestoreItinerari caricaDaFile(CaricaDati d, GestoreRistoranti r) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("itinerary_manager.ser"))) {
			GestoreItinerari g = (GestoreItinerari) ois.readObject();
			g.dati = d;
			g.ristoranti = r;
			g.rand = new Random();
			return g;
		} catch (Exception e) {
			System.out.println("Nessun salvataggio trovato, creo nuovo GestoreItinerari.");
			return new GestoreItinerari(d, r);
		}
	}

	// SALVA SU FILE
	void salva() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("itinerary_manager.ser"))) {
			oos.writeObject(this);
		} catch (Exception e) {
			System.err.println("Errore salvataggio itinerari: " + e.getMessage());
		}
	}

	// CREA ITINERARIO CON CITTÀ CASUALE
	Itinerario creaRandom(Utente u, String n, LocalDate i, LocalDate f, int p, int num) {
		return crea(u, n, i, f, p, num, null);
	}

	// CREA ITINERARIO PER UNA CITTÀ SPECIFICA
	Itinerario creaConCitta(Utente u, String n, LocalDate i, LocalDate f, int p, Citta c) {
		return crea(u, n, i, f, p, 1, c);
	}

	// METODO INTERNO CHE COSTRUISCE L'ITINERARIO CON LE TAPPE
	private Itinerario crea(Utente ut, String n, LocalDate ini, LocalDate fin, int p, int num, Citta fissa) {
		Itinerario it = new Itinerario(n, ut, ini, fin, p);

		// SELEZIONA LE CITTÀ
		List<Citta> sel = new ArrayList<Citta>();
		if (fissa != null) {
			sel.add(fissa);
		} else {
			List<Citta> tutte = dati.getTutte();
			int toPick = num;
			if (toPick > tutte.size()) {
				toPick = tutte.size();
			}
			while (sel.size() < toPick && !tutte.isEmpty()) {
				Citta c = tutte.get(rand.nextInt(tutte.size()));
				if (!sel.contains(c)) {
					sel.add(c);
				}
			}
		}

		if (sel.isEmpty()) {
			return it;
		}

		// DISTRIBUISCE I GIORNI E CREA LE TAPPE
		LocalDate g = ini;
		int gXCit = it.getDurata() / sel.size();
		int extra = it.getDurata() % sel.size();

		for (int i = 0; i < sel.size(); i++) {
			Citta c = sel.get(i);
			int giorni = gXCit;
			if (extra > 0) {
				giorni++;
				extra--;
			}

			String h;
			if (c.hotel.isEmpty()) {
				h = "N/D";
			} else {
				h = c.hotel.get(rand.nextInt(c.hotel.size()));
			}

			for (int j = 0; j < giorni && !g.isAfter(fin); j++) {
				String a;
				if (c.attrazioni.isEmpty()) {
					a = "N/D";
				} else {
					a = c.attrazioni.get(rand.nextInt(c.attrazioni.size()));
				}
				it.addTappa(new Tappa(c, h, ristoranti.getRistoranteCasuale(c.nome), a, g));
				g = g.plusDays(1);
			}
		}

		it.calcolaPrezzo();

		// SALVA L'ITINERARIO NELLA MAPPA
		if (!mappa.containsKey(ut.username)) {
			mappa.put(ut.username, new ArrayList<Itinerario>());
		}
		List<Itinerario> listaUtente = mappa.get(ut.username);
		for (int i = listaUtente.size() - 1; i >= 0; i--) {
			if (listaUtente.get(i).id.equals(it.id)) {
				listaUtente.remove(i);
			}
		}
		listaUtente.add(it);
		salva();
		return it;
	}

	// RESTITUISCE GLI ITINERARI DI UN UTENTE
	List<Itinerario> getItinerari(Utente u) {
		if (!mappa.containsKey(u.username)) {
			mappa.put(u.username, new ArrayList<Itinerario>());
		}
		return mappa.get(u.username);
	}

	// ELIMINA UN ITINERARIO
	void elimina(Itinerario it) {
		if (mappa.containsKey(it.utente.username)) {
			List<Itinerario> lista = mappa.get(it.utente.username);
			for (int i = lista.size() - 1; i >= 0; i--) {
				if (lista.get(i).id.equals(it.id)) {
					lista.remove(i);
				}
			}
		}
		salva();
	}
}

class CaricoImmagini {
	// CACHE PER NON RICARICARE LA STESSA IMMAGINE PIU' VOLTE
	private static Map<String, ImageIcon> cache = new HashMap<String, ImageIcon>();

	// CARICA UN'IMMAGINE DA URL E LA RIDIMENSIONA
	static ImageIcon carica(String url, int w, int h) {
		String key = url + "_" + w + "x" + h;
		if (cache.containsKey(key)) {
			return cache.get(key);
		}
		try {
			Image img = ImageIO.read(new URL(url));
			if (img != null) {
				ImageIcon ic = new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
				cache.put(key, ic);
				return ic;
			}
		} catch (Exception e) {
			System.err.println("Errore caricamento immagine: " + e.getMessage());
		}
		return null;
	}
}