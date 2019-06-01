package org.kumoricon.registration.print.formatter.badgeimage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BadgeCreatorFull implements BadgeCreator {
    private static final int DPI = 150;
    private static final int BADGE_WIDTH = 5*DPI;
    private static final int BADGE_HEIGHT = 4*DPI;
    private static Font badgeFont;
    private static Font nameFont;
    private static String badgeResourcePath = "/usr/local/kumoreg/badgeResources";

    @Override
    public byte[] createBadge(AttendeeBadgeDTO attendee) {
        BadgeImage b = new BadgeImage(BADGE_WIDTH, BADGE_HEIGHT, DPI);

        drawBadgeTypeStripe(b, attendee);
        drawAgeColorStripe(b, attendee);
        drawLargeName(b, attendee);
        drawSmallName(b, attendee);
        drawBadgeNumber(b, attendee);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(b.getImage(), "png", baos);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void drawAgeColorStripe(BadgeImage b, AttendeeBadgeDTO attendee) {
        Color bgColor = Color.decode(attendee.getAgeStripeBackgroundColor());
        Color fgColor = getForegroundColor(attendee.getAgeStripeBackgroundColor());
        Rectangle ageBackground = new Rectangle(60, 455, 630, 80);
        b.fillRect(ageBackground, bgColor);

        Rectangle ageTextBoundingBox = new Rectangle(70, 450, 610, 80);
        b.drawStretchedCenteredString(BadgeImage.buildTitleString(attendee.getAgeStripeText()), ageTextBoundingBox, getBadgeFont(), fgColor);
    }

    private static void drawLargeName(BadgeImage b, AttendeeBadgeDTO attendee) {
        // If fan name exists, draw it here. Otherwise draw the full name here
        String name;
        Rectangle largeNameBg;
        if (attendee.getFanName() != null && !attendee.getFanName().trim().equals("")) {
            largeNameBg = new Rectangle(70, 275, 360, 70);
            name = attendee.getFanName();
        } else {
            // Draw regular name in the combined name and fan name spaces
            largeNameBg = new Rectangle(70, 275, 360, 130);
            name = attendee.getFirstName() + " " + attendee.getLastName();
        }

        b.drawStretchedLeftAlignedString(name, largeNameBg, getNameFont(),Color.BLACK);
    }

    private void drawSmallName(BadgeImage b, AttendeeBadgeDTO attendee) {
        // If Fan Name exists, draw the full name here (on the second line)
        String fanName = attendee.getFanName();
        if (fanName != null && !attendee.getFanName().trim().isEmpty()) {
            Rectangle nameBg = new Rectangle(70, 340, 360, 40);
            b.drawStretchedCenteredString(attendee.getFirstName() + " " + attendee.getLastName(), nameBg, getNameFont(), Color.BLACK);
        }
    }

    private static void drawBadgeTypeStripe(BadgeImage b, AttendeeBadgeDTO attendee) {
        if (attendee != null) {
            Color bgColor = Color.decode(attendee.getBadgeTypeBackgroundColor());
            Color fgColor = getForegroundColor(attendee.getBadgeTypeBackgroundColor());
            Rectangle badgeType = new Rectangle(60, 70, 630, 80);
            b.fillRect(badgeType, bgColor);

            Rectangle textBoundingBox = new Rectangle(80, 80, 610, 70);
            b.drawStretchedCenteredString(BadgeImage.buildTitleString(attendee.getBadgeTypeText()), textBoundingBox, getBadgeFont(), fgColor);
        }
    }

    private static void drawBadgeNumber(BadgeImage b, AttendeeBadgeDTO attendee) {
        String badgeNumber = attendee.getBadgeNumber();
        Color fgColor = getForegroundColor(attendee.getAgeStripeBackgroundColor());
        Rectangle badgeNumberBounds = new Rectangle(85, 465, 100, 80);

        if (badgeNumber.length() == 8) {
            String[] lines = {badgeNumber.substring(0, 3), badgeNumber.substring(3)};
            b.drawCenteredStrings(lines, badgeNumberBounds, getBadgeFont(), fgColor);
        } else {
            b.drawStretchedCenteredString(badgeNumber, badgeNumberBounds, getBadgeFont(), fgColor);
        }
    }


    /**
     * Fallback font and font for fan name. Returns a font family to include as
     * many foreign/weird characters as possible
     */
    private static Font getNameFont() {
        if (nameFont == null) {
            nameFont = new Font("Dialog", Font.BOLD, 36);
        }
        return nameFont;
    }

    private static Font getBadgeFont() {
        if (badgeFont == null) {
            Path fontPath = Paths.get(badgeResourcePath, "/Bitstream - BankGothic Md BT Medium.ttf");
            try (InputStream stream = new FileInputStream(fontPath.toFile())) {
                badgeFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(36f);
            } catch (FontFormatException | IOException e) {
                badgeFont = getNameFont();
            }
        }
        return badgeFont;
    }

    private static void save(BufferedImage image, String filename) {
        Path output = Paths.get(filename);
        try {
            ImageIO.write(image, "png", output.toFile());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Returns the best text color for the given background color. Ideally chooses white text on dark
     * backgrounds and black text on light backgrounds. Uses the formula from
     * http://en.wikipedia.org/wiki/HSV_color_space%23Lightness
     * Based on https://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color
     * @param backgroundColor HTML color code. Ex: #0C0AB1
     * @return Color (#FFFFFF for white or #000000 for black)
     */
    private static Color getForegroundColor(String backgroundColor) {
        Color background = Color.decode(backgroundColor);
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;

        if (a < 0.5) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }
}
