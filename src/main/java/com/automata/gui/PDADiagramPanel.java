package com.automata.gui;

import com.automata.model.PDA;
import com.automata.model.PDATransition;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple Swing renderer for a PDA.
 * - Circular layout (automatic)
 * - Draws states, start arrow, double circle for accepting states
 * - Edge labels are built defensively from PDATransition fields so combined tokens like "()"
 *   or "(E)" are shown as separate atomic tokens "(" "E" ")".
 *
 * Add the panel to your GUI and call setPDA(pda) after you generate it.
 */
public class PDADiagramPanel extends JPanel {
    private PDA pda;
    private Map<String, Point> statePositions = new LinkedHashMap<>();
    private int nodeRadius = 28;
    private Font labelFont = new Font("SansSerif", Font.PLAIN, 12);

    // Token pattern: multi-char identifiers OR any single non-whitespace symbol
    // (keeps "id" together, but splits "(E)" into "(", "E", ")")
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*|\\S");

    public PDADiagramPanel() {
        setPreferredSize(new Dimension(800, 420));
        setBackground(Color.WHITE);
    }

    public void setPDA(PDA pda) {
        this.pda = pda;
        computeLayout();
        repaint();
    }

    private void computeLayout() {
        statePositions.clear();
        if (pda == null) return;

        List<String> states = new ArrayList<>(pda.getStates());
        int n = states.size();
        if (n == 0) return;

        int w = getWidth() > 0 ? getWidth() : 800;
        int h = getHeight() > 0 ? getHeight() : 420;
        int cx = w / 2;
        int cy = h / 2;
        int radius = Math.min(w, h) / 2 - 120;
        if (radius < 100) radius = 100;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            int x = cx + (int) (radius * Math.cos(angle));
            int y = cy + (int) (radius * Math.sin(angle));
            statePositions.put(states.get(i), new Point(x, y));
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        computeLayout();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (pda == null) {
            g.setColor(Color.DARK_GRAY);
            g.drawString("No PDA to display", 10, 20);
            g.dispose();
            return;
        }

        List<PDATransition> transitions = pda.getTransitions();

        // draw edges first
        g.setFont(labelFont);
        for (PDATransition t : transitions) {
            String from = t.getFromState();
            String to = t.getToState();
            if (from == null || to == null) continue;
            Point pFrom = statePositions.get(from);
            Point pTo = statePositions.get(to);
            if (pFrom == null || pTo == null) continue;

            // build normalized label defensively (prevents "()" artifacts)
            String label = buildNormalizedLabel(t);
            drawArrow(g, pFrom.x, pFrom.y, pTo.x, pTo.y, label);
        }

        // states
        Set<String> accepting = pda.getAcceptingStates();
        String startState = pda.getInitialState();

        for (Map.Entry<String, Point> e : statePositions.entrySet()) {
            String s = e.getKey();
            Point p = e.getValue();
            boolean isAccept = accepting.contains(s);
            boolean isStart = (startState != null && startState.equals(s));

            int x = p.x - nodeRadius;
            int y = p.y - nodeRadius;
            Ellipse2D circle = new Ellipse2D.Double(x, y, nodeRadius * 2, nodeRadius * 2);
            g.setColor(Color.WHITE);
            g.fill(circle);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.draw(circle);
            if (isAccept) {
                Ellipse2D inner = new Ellipse2D.Double(x + 6, y + 6, (nodeRadius * 2) - 12, (nodeRadius * 2) - 12);
                g.draw(inner);
            }

            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(s);
            int tx = p.x - tw / 2;
            int ty = p.y + fm.getAscent() / 2 - 2;
            g.drawString(s, tx, ty);

            if (isStart) {
                int ax = p.x - nodeRadius - 28;
                int ay = p.y;
                drawArrow(g, ax, ay, p.x - nodeRadius, p.y, "");
            }
        }

        g.dispose();
    }

    /**
     * Build an edge label from the transition by normalizing tokens. Format:
     *   <input>, <stackTop> -> <stackPush>
     * Uses "ε" for epsilon / empty.
     */
    private String buildNormalizedLabel(PDATransition t) {
        String in = normalizeTokenString(t.getInputSymbol(), true);
        String top = normalizeTokenString(t.getStackTop(), true);
        String push = normalizeTokenString(t.getStackPush(), true);

        return String.format("%s, %s -> %s", in, top, push);
    }

    /**
     * Normalize a raw transition field (may be null, "epsilon", a space-separated list,
     * or a combined token like "(E)" or "()" ). Returns a single string where atomic tokens
     * are separated by single spaces. If the field is null/empty/epsilon, returns "ε".
     *
     * Examples:
     *   "(E)" -> "( E )"
     *   "id"  -> "id"
     *   ") E (" -> ") E ("
     */
    private String normalizeTokenString(String raw, boolean treatEpsilonAsSymbol) {
        if (raw == null) return "ε";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "ε";
        if (trimmed.equalsIgnoreCase("epsilon") || trimmed.equals("ε")) {
            return "ε";
        }

        // First split by whitespace to handle already tokenized inputs.
        String[] parts = trimmed.split("\\s+");
        List<String> tokens = new ArrayList<>();

        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            Matcher m = TOKEN_PATTERN.matcher(p);
            int found = 0;
            while (m.find()) {
                tokens.add(m.group());
                found++;
            }
            if (found == 0) {
                // fallback: add original part if pattern didn't match
                tokens.add(p);
            }
        }

        if (tokens.isEmpty()) return "ε";
        return String.join(" ", tokens);
    }

    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2, String label) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int sx = x1;
        int sy = y1;
        int ex = x2;
        int ey = y2;

        // shorten so arrow does not go into node
        double len = Math.hypot(dx, dy);
        if (len > 0) {
            double shorten = nodeRadius;
            sx = x1 + (int) (shorten * Math.cos(angle));
            sy = y1 + (int) (shorten * Math.sin(angle));
            ex = x2 - (int) (shorten * Math.cos(angle));
            ey = y2 - (int) (shorten * Math.sin(angle));
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.4f));
        g.drawLine(sx, sy, ex, ey);

        // arrow head
        AffineTransform old = g.getTransform();
        g.translate(ex, ey);
        g.rotate(angle);
        Path2D arrowHead = new Path2D.Double();
        arrowHead.moveTo(0, 0);
        arrowHead.lineTo(-8, -6);
        arrowHead.lineTo(-8, 6);
        arrowHead.closePath();
        g.fill(arrowHead);
        g.setTransform(old);

        // label near midpoint, offset perpendicular
        if (label != null && !label.isEmpty()) {
            int mx = (sx + ex) / 2;
            int my = (sy + ey) / 2;
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(label);
            double px = -(ey - sy);
            double py = (ex - sx);
            double plen = Math.sqrt(px * px + py * py);
            if (plen == 0) plen = 1;
            px = px / plen * 12;
            py = py / plen * 12;
            int lx = (int) (mx + px) - tw / 2;
            int ly = (int) (my + py) + fm.getAscent() / 2 - 2;

            // label background for readability
            Color oldColor = g.getColor();
            g.setColor(new Color(255, 255, 255, 230));
            g.fillRect(lx - 4, ly - fm.getAscent(), tw + 8, fm.getHeight());
            g.setColor(oldColor);
            g.drawString(label, lx, ly);
        }
    }

    public BufferedImage toImage() {
        int w = Math.max(getWidth(), 800);
        int h = Math.max(getHeight(), 420);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        // ensure layout reflects size
        this.setSize(w, h);
        this.paint(g2);
        g2.dispose();
        return img;
    }
}
