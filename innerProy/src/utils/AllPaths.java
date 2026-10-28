package utils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralizes immutable card metadata and images. Each unique image is
 * decoded exactly once per application run, then reused by enum instances.
 */
public final class AllPaths {

    private AllPaths() {}

    public static final class Resources {

        private Resources() {}

        public static final class DefaultCardModels {

            private DefaultCardModels() {}

            public enum ActorsData {
                DAVID("David", Color.BLUE, "DAVID.png"),
                LUKA("Luka", new Color(128, 0, 128), "LUKA.png"),
                GARAIZAR("Garaizar", Color.PINK, "GARAIZAR.png"),
                ANDRADA("Andrada", Color.RED, "ANDRADA.png"),
                ANTAL("Antal", Color.GREEN, "ANTAL.png"),
                BRINGAS("Bringas", Color.YELLOW, "BRINGAS.png");

                private final String name;
                private final Color color;
                private final BufferedImage mainBackgroundImage;
                private final BufferedImage frontCircleImage;

                ActorsData(String name, Color color, String filename) {
                    this.name = name;
                    this.color = color;
                    this.mainBackgroundImage = loadImage("ActorCard/" + filename, null);
                    this.frontCircleImage = createActorIcon(color);
                }

                public String getName() {
                    return name;
                }

                public Color getColor() {
                    return color;
                }

                public BufferedImage getMainBackgroundImage() {
                    return mainBackgroundImage;
                }

                public BufferedImage getFrontCircleImage() {
                    return frontCircleImage;
                }
            }

            public enum WeaponsData {
                MACHETE("Machete", "MACHETE.png", WeaponPng.MACHETE),
                TIZA("Tiza", "TIZA.png", WeaponPng.TIZA),
                TECLADO("Teclado", "TECLADO.png", WeaponPng.TECLADO),
                CABLE("Cable", "CABLE.png", WeaponPng.CABLE),
                SILLA("Silla", "SILLA.png", WeaponPng.SILLA),
                DESTORNILLADOR("Destornillador", "DESTORNILLADOR.png", WeaponPng.DESTORNILLADOR);

                private final String name;
                private final BufferedImage mainBackgroundImage;
                private final BufferedImage frontCircleImage;

                WeaponsData(String name, String filename, String circlePngBase64) {
                    this.name = name;
                    this.mainBackgroundImage = loadImage("ActorWeapon/" + filename, null);
                    this.frontCircleImage = loadImage("icon/" + name, circlePngBase64);
                }

                public String getName() {
                    return name;
                }

                public BufferedImage getMainBackgroundImage() {
                    return mainBackgroundImage;
                }

                public BufferedImage getFrontCircleImage() {
                    return frontCircleImage;
                }

                public BufferedImage getFrontCircleBufferedImage() {
                    return frontCircleImage;
                }
            }

            public enum RoomsData {
                CRAI("CRAI", "CRAI.png", "Machete", WeaponPng.MACHETE),
                BANOS("Baños", "Baños.png", "Tiza", WeaponPng.TIZA),
                CAFETERIA("Cafetería", "Cafetería.png", "Teclado", WeaponPng.TECLADO),
                SALA_DE_MAQUINAS("Sala de máquinas", "Sala de máquinas.png", "Cable", WeaponPng.CABLE),
                LABORATORIOS("Laboratorios", "Laboratorios.png", "Silla", WeaponPng.SILLA),
                DEUSTOTECH("DeustoTech", "DeustoTech.png", "Destornillador", WeaponPng.DESTORNILLADOR),
                AULA_E208("Aula E208", "Aula E208.png", "Machete", WeaponPng.MACHETE),
                // Decanato.png is not included in the supplied project.
                // Reuse the CRAI background until a dedicated image exists.
                DECANATO("Decanato", "CRAI.png", "Tiza", WeaponPng.TIZA),
                POLIDEPORTIVO("Polideportivo", "Polideportivo.png", "Teclado", WeaponPng.TECLADO);

                private final String name;
                private final BufferedImage mainBackgroundImage;
                private final BufferedImage frontCircleImage;

                RoomsData(String name, String filename, String iconId, String circlePngBase64) {
                    this.name = name;
                    this.mainBackgroundImage = loadImage("ActorRoom/" + filename, null);
                    this.frontCircleImage = loadImage("icon/" + iconId, circlePngBase64);
                }

                public String getName() {
                    return name;
                }

                public BufferedImage getMainBackgroundImage() {
                    return mainBackgroundImage;
                }

                public BufferedImage getFrontCircleImage() {
                    return frontCircleImage;
                }

                public BufferedImage getFrontCircleimage() {
                    return frontCircleImage;
                }
            }

            /**
             * shared image loader. It passes null for an image stored on disk, or
             * Base64 for an embedded PNG. Same key will always return the same
             * BufferedImage instance which is more efficient because that way reading and decoding will happen only once.
             */
            private static final Map<String, BufferedImage> IMAGE_CACHE = new ConcurrentHashMap<>();

            private static BufferedImage loadImage(String resourceKey, String base64) {
                return IMAGE_CACHE.computeIfAbsent(resourceKey, key -> base64 == null ? readImageFile(key) : decodePng(base64));}

            private static BufferedImage readImageFile(String relativePath) {
                String classpathPath = "/cardAspects/" + relativePath;
                try (InputStream in = AllPaths.class.getResourceAsStream(classpathPath)) {
                    if (in != null) {
                        return decodeImage(in, classpathPath);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read " + classpathPath, e);
                }

                Path cwd = Path.of("").toAbsolutePath().normalize();
                for (Path directory = cwd; directory != null; directory = directory.getParent()) {
                    for (Path candidate : new Path[] {
                            directory.resolve("ressources/cardAspects").resolve(relativePath),
                            directory.resolve("innerProy/ressources/cardAspects").resolve(relativePath)}){

                        if (Files.isRegularFile(candidate)) {
                            try (InputStream in = Files.newInputStream(candidate)) {
                                return decodeImage(in, candidate.toString());
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot read " + candidate, e);
                            }
                        }
                    }
                }

                throw new IllegalStateException("Card image not found: " + relativePath);
            }

            private static BufferedImage decodePng(String base64) {
                byte[] bytes = Base64.getDecoder().decode(base64);
                try (InputStream in = new ByteArrayInputStream(bytes)) {
                    return decodeImage(in, "embedded PNG");
                } catch (IOException e) {
                    throw new IllegalStateException("Invalid embedded PNG", e);
                }
            }

            private static BufferedImage decodeImage(InputStream input, String source) throws IOException {
                BufferedImage image = ImageIO.read(input);
                if (image == null) {
                    throw new IOException("Unsupported image format: " + source);
                }
                return image;
            }

            private static BufferedImage createActorIcon(Color color) {
                BufferedImage result = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = result.createGraphics();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(color.darker().darker());
                    g.fillOval(0, 0, 128, 128);
                    g.setColor(new Color(241, 225, 181));
                    g.fillOval(48, 25, 32, 32);
                    g.fillRoundRect(30, 65, 68, 43, 24, 24);
                } finally {
                    g.dispose();
                }
                return result;
            }

            private static final class WeaponPng {
                private WeaponPng() {}
                private static final String MACHETE = "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";
                private static final String TIZA = "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";
                private static final String TECLADO = "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";
                private static final String CABLE = "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";
                private static final String SILLA = "iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAAwFBMVEWZTg8AAADy47ebUBHu26+iVBOpVAD368DFmGP/AACZTg/auomZTQ+XSw6ZTg9/fwCZTg+ZTg9+PgCoZirOpnKYUg6bUA+7h0/ix5ibUA+wdDqZUA9VVQDdwZB/AADCklyXTg7Ajlf/fwB/VQCaUA+1fUSZMwCbUA+aUBCZZgCjXiHUsX7//wCPPAC/PwClSw+qVSr68MZ/KgBtSABmZgDlzqAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBQWFWAAAAQHRSTlP+AP/9//4D//8Bjv9LGLECzG4E//8XjP//0P9NA/8C/yr/Agau/wUzdwX//wH/BBEG/wYHBf8AAAAAAAAAAAAARlWtuQAABqhJREFUeNrNm2lD4zgMhmU7cXGauwc9oHSAAWbn2tnj//+1tZ1QSjeOX0NCxx/gAyF6IsmyZFvEQsZO1va3zPbbapnkeV6W+keyrLb7xZX9Uy13Qa8k/FElC/2zyLZP92siEb8agmidP6V7+0itRgCQt+ZHmpTUyjsdlojKJF2YB+WwAMq8L0vvRbfs1xTivmFQgwEY8TLNySP8CELk6RcQgSDxi6oUmPQDw7paQAheAC0+W+oXUuDQ/7HM7L+/C6AVL+gNQ0AI5NH+P9UbxTcI4ulvjx2o//PT9dvFNwjrtF8J1CffaJ/eOWKh7SDfAFDYz6cBhlVCEQog2V0iBA0yhFj+5VSCA6Bm2TCf/6yEPatDACRLxYDyjSekDgLqll8Npf4XM1TdVugAULesimnwESesUAiAKlgygnwXAX2YfE2wZDvlA1BXo8m3BFfKAyBHlG+tIPsB6lHlW4K6D6DW/i/GBBBxdUJAp/GHRh46IkkXQMEyMT6AyF6tTHScfmRlTKOPuMyOUxT6qAngmgr0oQ7QWmF7RHAA2BULwtJ+90AB1j9fYjKFGcAjBGQ4NgIFGUD8ryg9rVAFOhdvTwCKP4EZIGg2X0XusZrHGEFcyuI1gERSAEGbaML7xiSagQSH9KQBUCrz/6P+/ohH/YNHmA4EZd/UEQDkgYLmk0h/pfv7OY8mF5grHvyQAhQgVlx/4oV76L/yFUGuKGih1AEAUYCgy8h+4XfnI9+NhqJpNEXM8KwCA6CYPwZpBzBaNip2x6ELYyLOf80hFfxkqgWo2ROggA2PWgDnIxZAe+IUccQ2M2hMUAIAccQhAP3MDQRQ3rUmkGyLBGGaGhcEADgYDJrURANcsRwCMK9HTIBZQKsg16I1wI5lBC0DnzkIcAkuqyS1cNKegFigiYOYD/wBLkmxKVhJJ2g5mAitEADYAtYGBSPFJKoxHWcQgEsUQEdDpgibA00o5BHiAzEKYGwgCa6FmsWQz/sB+h7orJO0D5QogOi3cLtafg4AKLUPYJPwBaBPw9fm7zMcwDgBsTQmHECPjUPA83KFu4B1AoKrkQYg4jdOgFAftE5AcBR4BnA6gc5XgjWQM3ooA00Q9VogEKD8QRkeNhoNDAmgc1Pa4w83Grh0+sA8GIDokeCKtJmGPZGQNEEUCCBSgvckW4CbnpzwZhIKEFe0DAPQkZYG1ECcUMimhIl0/HrqHIe6IAQgj3Ev/NTkfD2jSdsDAPIAAD2mVoi7MuR8JdoyfmgAXRdo3V9z31hpS1x/wjOCnODFOPaU5kdF6gYHKAmPxBc8AqRHAUmhBcDXolkEfX9AUhjmhE1phBCMNAtMeYqI7w3WHQAhgWjFvdOQB+VkJhAtgwPRxDWs+F/TwFAcckAmTBxw7tHc2OV4OgtcjLYh6cNXG+ydb4vQvYHj5Xgf8vhKS/jqXo9XPCgINAkJnJK1pZHbybWCtI+sghSgxcNJ6fM8/OwGsAlJ0CQoazwtb6pTzmdugH7ALoD7gMKkrf3cGY/PRN07ZXhp1sbi6773h3phvA0vTnuqb9EkhSLArU1xipfnVsOf+gBsqJwFZAO6PK/ZEgUw22R80wewCVuN7QZFwBaNb5aJJhbOw7ZokJ3qIwv/K7yJ+xSvNjOmwrbpPG/3TdTObTpWYxMR0a8vVHVvVII2sOW3x8PCvNBuE4ZsVvu/LswLtQVku12PnVnafdD+fWBhvfAazQVaAMbuoEPLaW82chwL47ADC3txA8tGfFOstdMm8MhGAesBttQ1njq5hA6tssOhFXJshzm4f8E6DsPy6ODSOxO1cpGFBvKUdg4eHVwCKgCX2jYW+s9MXh/dMvUtgz5tCkaLjV+hrw+vvcf3qHHbeOktDk6P74ELDMKTjQQ9F5dfTi4wsFtPODS65VMBTFfkuY4rHIAfzjYCmt/+57ousTBVSN8cJ/ByhO85sc46rvGYI+RB7uj4nxNbdvU7XuViSsr1h1xmk47LbGe/zmdyow+50Hj1+17pZOzH2Jdaq/5Lree/1jvyxebEf7GZqd14V7sT4Gq3vVy+POfldtvcc9br/edvcBirxUMGNrmU52xyMTp4WA7X5pM8hLb5nL/R6fytXm2zW3nGZjcbEep3tvtVd+9o92sbHhN6c8Nj8s6Gx/e1fBrjuybf+E2vYl1lwzS9vqntl/LtcG2/h8bnRZpDjc80eONz4wsNg7/1e5uN0PrdqKE2BeUuS6u87Gp+L/Mq3avRmt9bUzReXWSPaZW8tP8nVfqYFW37vwp65X+CBV93z/fXhAAAAABJRU5ErkJggg==";
                private static final String DESTORNILLADOR = "iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAAwFBMVEWniREAAADx4bWxkROoiRHs26uqqgCniRHSu3D//wCniRF/fwCoihGniRGzmCyniRH158CqVQDcyIe4fwHCqU3l0prXwnv/AADJsVz/fwDhzZGmiBC9pEO6oTyskhGXlwCXZwD/qgBVVQCqfyqqqipmZgCbfw6fhw+2kSTMmQDUqioAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAANbaQyAAAAQHRSTlP+AP/+jv8DS/8BGALNsP9v/wP/Bv///wH/Av8u//8WBQUDAwYGBRIgBwUGAAAAAAAAAAAAAAAAAAAAAAAAAAAAHeIMwwAABbdJREFUeNrNm2l3syoQgEcUFfeoiVnatO92t///Ay+YqGhRFjGWD+05rck8zgzDADPg6IzYr9rffnQLcs9L0zRJ6A/Py4NbdGj/Vfmx1leC+qPEvzCGKMjTBOhw2eh/ASQpxWDSLzHZAODApEeBl0AncDravydeELGnfbsAxG+lpyAWPcaAtGXwiTUAJt5Xks4xfCgigJL4KKeaB41BbZFXSghSACbe05PeMXhR+/FVAAdT8QPCYQUAOTifuan4B0L+6RyIKQBVX5CsEN8iJMGyHWBJfuXBSvmtHaolglmA2MLrc0qIdQF8J/bA2vDiWSXA3PtHdl6/U8JtTgcw8/4BWJTPPCGYIRABNAcnB+sjdw6NGgC5OLlrH8D1nAtRAWhix9tAfksgSBRA8P7byJ8hmAI0PzeT3xJ8ictTgB8bym8JfiwDVJvKbwmqJYB4E/8fEeSTeADj1T+AzUcwzhBgHH/hBSMa6QD45O+fxN1evpt88Kki8AuA575CA9QRfRGAyAHeyjrb2A16gDgWOECGEC7P1hmS+xARYdkAR4RQgY7n7YwAXAYgGCfEBi5C60YgE4D4t3AGnJl4jBGqLc+EX/EYwJ8JgcwJcKuFzHZA9HkAQu4zD2L0kI+wZVeMCOEA5kNA+ZSP0HEbP4SHAmZjcI36UW6iApDFwIHA7lToVMAAmsVFiNOB3XAQOc0TQJIFcARv9jOD1gRVsvjoWw9gdSok1dMEvjQN2YggYF5AAX46qWwZfu8JrhZtkLJFEagh7vKH37twUJws50YMIFDIQzJsfzK6AU2RGUCqkgj1BPbWJWoDqgGimokOBO8WbUDAV7LAiABbtIEPGnuhjoC5gZXpyPZJ4FzUc/HsuTjj+oqxBVdwEzYLdDYjGS56T7DijBEFCHQ2A9m1sLkyUCcA3d1IOKiApmlZXZ/P5nsH6gSgFgV4ggHgeqUeWVCdHMPMNBLAh+Z+MEOiUaBTZuSFFdw1PxMWSIyAjdKVCG6an6i5eTBBKA2UcAPtI4krmiMwyRYC0D4TdY/zBPozMwf9MwEajWYBdKOT6xkAQFYiWwQUIDU4FQkXAPSs4KZGAHgJQMsTzQDe0eI46gEk+gqoi0UArawxMQEIlwG03CAxMcHgg8dypRuY+UCvARyGVyFBuS3AuQcow/C4yg0ogEEg4raKpxkC1dTdMBJiOYGiG1AAkws6zvVmCRTdIAeTG4I3NCYQRcZCLT8JtBOSdvDvTKfCSRiblYxwA6M7ihopECjt4CL4NLqkGAWg4wyB/CiBJaWOyXI4yY3nCGp5GNDfmEyDUUdQCgiw7KvdXHdrxhlhlB1fKcFX+dKA2G7NTG/KxrNfSCBPDdjmNDa8KsvGOi+FBJl8e25+WatAUNTyAwrf1AmG84qB4Kq3KrZHNGTFdWk22qPgrwQyL2SHVKrHdOJXKAu0tDAtAzyO6ZzK3AZ0nJYJaoWDSrLuynq0SZkSSLICagHFw2r1hWm8OJcqh9UKx/Uak4ESlKqJWXdc7zRVso6AnwxDpo4lJ+v9hYWFwo1SuFVZXou4K5tmfeVEaJASDZdWNkoXztO3lzkAf223dHGpPhnw5NBMlhLyF5dWqjdGp7jo5IKSAp4ADbFRP1MfEWMoClTKN0YRaVSu73WPLsKyLE8KNR/T63s6FX8lLymh6TKRfycFDDQoB/DC8bWE42VVRCMP5AFIfH/Z+yeRoIznNZVsnQEO37GUyyH+f68pZvs9U8y2eznf/gWN+5d0vqCoNV8uat2/rJcVtm9b2NzsW9p9ITsXtzdK5f2Hfcv7929w2KrFw9dpcqlsN7n8rdfkwnTwYbPN51O3zWf/RqdWCZGdVq+/jFq9vkGzW9ts+Wdtu1+1ot1v/4ZHCy2f0i7oDZteIztNr/u3/X6DxueHOzJnuu/V+v3cvCk0v5PNmt+fpniklLGo/T9+tv8Tra/8H2oXTEw0EPCBAAAAAElFTkSuQmCC";
            }
        }
    }
}
