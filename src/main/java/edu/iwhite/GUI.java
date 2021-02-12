package edu.iwhite;

import javax.swing.*;
import java.awt.*;

public class GUI {

    private static final int MARGIN = 20;

    private final JFrame frame;
    private final JAutoComplete jac;
    private final JTextArea text;

    /**
     * Returns GridBagConstraints
     */
    private static GridBagConstraints position(int x, int y) {
        return position(x, y, 1);
    }

    /**
     * Utility method for position(). Returns GridBagConstraints with a given width.
     */
    private static GridBagConstraints position(int x, int y, int width) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.insets = new Insets(MARGIN, MARGIN, MARGIN, MARGIN);
        constraints.gridwidth = width;

        return constraints;
    }

    /**
     * Constructs a GUI which takes a HashTable of Businesses and a HashTable of Reviews. GUI
     * contains a JFrame with a content panel, a JTextArea (for printing suggested Business)
     * and a JAutoComplete (extension of JTextField).
     */
    public GUI(HashTable<String, Business> businesses, HashTable<String, FrequencyTable> reviews) {
        frame = new JFrame("Business Searcher");
        frame.setLayout(new GridBagLayout());

        text = new JTextArea("Suggested Business: \n");
        text.setEditable(false);
        text.setRows(5);
        text.setColumns(50);
        text.setLineWrap(true);

        // create list of names of business with at least one review
        jac = new JAutoComplete(reviews.keySet(), businesses);
        jac.setEditable(true);
        jac.setClickListener(selectedID -> {
            double max = Double.MIN_VALUE;
            Business most = null;

            // compare businesses in HashTable businesses using FrequencyTable.compare() by finding the most
            // similar business
            for (String businessID : businesses.keySet()) {
                if (!businessID.equals(selectedID)) {
                    double similarity = FrequencyTable.compare(reviews.get(businessID), reviews.get(selectedID));
                    if (similarity > max) {
                        max = similarity;
                        most = businesses.get(businessID);
                    }
                }
            }

            // fills JTextArea with one selected business and a copyable link to Google Maps
            text.setText(
                    "Suggested Business: \n" + most.getName() + "\n\n" +
                            "Location: " + "https://www.google.com/maps?q=" +
                            most.getLatitude() + "," + most.getLongitude()
            );
        });

        frame.getContentPane().add(jac, position(0, 0));
        frame.getContentPane().add(text, position(0, 1));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
