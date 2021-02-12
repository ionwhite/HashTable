package edu.iwhite;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.function.Consumer;

public class JAutoComplete extends JTextField {

    private static final int WIDTH = 20;
    private static final int MIN_CHARACTERS = 1;
    private static final int MAX_SUGGESTIONS = 10;

    private final Trie trie = new Trie();
    private final JPopupMenu popup = new JPopupMenu();
    private Consumer<String> listener;

    // sets ActionListener (used for a Click input) given some function
    public void setClickListener(Consumer<String> fn) {
        this.listener = fn;
    }

    private void updateAutoComplete() {
        if (super.getText().length() < MIN_CHARACTERS)
            popup.setVisible(false);
        else {
            popup.removeAll();

            trie.prefixedBy(super.getText(), MAX_SUGGESTIONS).forEach(option -> {
                final JMenuItem item = new JMenuItem(option.getName());
                item.addActionListener(event -> {
                    super.setText(option.getName());
                    if (listener != null) listener.accept(option.getBusinessID());
                });
                popup.add(item);
            });

            popup.pack();
            // subtract 20 pixels from x and y to removing 'floating' JTextField
            popup.show(this, this.getX() - 20, this.getY() + this.getHeight() - 20);
        }

        super.requestFocus();
    }

    public JAutoComplete(Collection<String> businessIDs, HashTable<String, Business> names) {
        super(WIDTH);

        super.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent event) {
                updateAutoComplete();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateAutoComplete();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateAutoComplete();
            }
        });

        businessIDs.forEach(id -> trie.add(names.get(id).getName(), names.get(id)));
    }

    private static final class Trie {

        private final Node root = new Node();

        public final void add(String value, Business business) {
            root.addChild(value, business, 0);
        }

        //returns List instead of Set for alphabetization
        public final List<Business> prefixedBy(String prefix, int limit) {
            if (limit < 0) throw new IllegalArgumentException();
            if (limit == 0) return List.of();

            int index = 0;
            Node node = root;
            while (node != null && index < prefix.length())
                node = node.children.get(prefix.charAt(index++));

            if (node == null) return List.of();

            final List<Business> possibilities = new ArrayList<>();
            //used Stack (Deque) instead of Queue for alphabetization
            final Deque<Node> deque = new ArrayDeque<>();
            deque.addFirst(node);

            while (possibilities.size() < limit && !deque.isEmpty()) {
                final Node p = deque.removeFirst();

                if (p.leaf)
                    possibilities.add(p.business);

                p.children.forEach((c, n) -> deque.addFirst(n));
            }

            return possibilities;
        }

        // added business to Node definition for use in this project
        private static final class Node {
            // changed HashMap to TreeMap to alphabetize (was originally sorted by length)
            final Map<Character, Node> children = new TreeMap<>(Comparator.reverseOrder());
            boolean leaf = false;
            Business business;

            final void addChild(String value, Business business, int index) {
                if (index < value.length()) {
                    if (!children.containsKey(value.charAt(index))) {
                        final Node child = new Node();
                        children.put(value.charAt(index), child);
                    }

                    children.get(value.charAt(index)).addChild(value, business, index + 1);
                } else {
                    leaf = true;
                    this.business = business;
                }
            }
        }
    }
}