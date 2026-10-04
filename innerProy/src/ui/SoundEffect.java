package ui;

import javax.sound.sampled.*;

public class SoundEffect {

    public static void playHover() {
        playTone(
                750,
                70,
                40
        );
    }

    public static void playClick() {

        playTone(
                950,
                100,
                70
        );
    }

    // SONIDO MISTERIOSO DE SCROLL
    public static void playScroll() {

        new Thread(() -> {

            try {

                int sampleRate = 44100;
                int duration = 220; // Muy corto

                byte[] buffer =
                        new byte[
                                sampleRate
                                        * duration
                                        / 1000
                                ];

                for (int i = 0;
                     i < buffer.length;
                     i++) {

                    double time =
                            (double) i / sampleRate;

                    double progress =
                            time / (duration / 1000.0);


                    // FRECUENCIA PRINCIPAL
                    // Baja rápidamente para crear
                    // el efecto de "whoosh"
                    double frequency =
                            900 - (700 * progress);


                    double wave1 =
                            Math.sin(
                                    2
                                            * Math.PI
                                            * frequency
                                            * time
                            );


                    // Segunda frecuencia muy suave
                    double wave2 =
                            Math.sin(
                                    2
                                            * Math.PI
                                            * frequency
                                            * 1.7
                                            * time
                            ) * 0.18;


                    // Ruido muy ligero para darle
                    // textura de movimiento
                    double noise =
                            (Math.random() * 2 - 1)
                                    * 0.08;


                    // ENVELOPE
                    // Entra rápidamente y desaparece
                    double fadeIn =
                            Math.min(
                                    1.0,
                                    time / 0.025
                            );

                    double fadeOut =
                            Math.max(
                                    0.0,
                                    1.0 - progress
                            );


                    double envelope =
                            fadeIn * fadeOut;


                    double sound =
                            (
                                    wave1
                                            + wave2
                                            + noise
                            )
                                    * 18
                                    * envelope;


                    buffer[i] =
                            (byte) sound;
                }


                AudioFormat format =
                        new AudioFormat(
                                sampleRate,
                                8,
                                1,
                                true,
                                false
                        );


                SourceDataLine line =
                        AudioSystem
                                .getSourceDataLine(
                                        format
                                );


                line.open(format);
                line.start();


                line.write(
                        buffer,
                        0,
                        buffer.length
                );


                line.drain();
                line.stop();
                line.close();


            } catch (Exception e) {

                System.out.println(
                        "No se pudo reproducir el sonido de scroll."
                );
            }

        }).start();
    }

    private static void playTone(
            double startFrequency,
            int duration,
            int volume
    ) {
        new Thread(() -> {

            try {

                int sampleRate = 44100;

                byte[] buffer =
                        new byte[
                                sampleRate
                                        * duration
                                        / 1000
                                ];

                for (int i = 0;
                     i < buffer.length;
                     i++) {

                    double time =
                            (double) i
                                    / sampleRate;

                    double frequency =
                            startFrequency
                                    - (
                                    startFrequency
                                            * 0.45
                                            * time
                                            / (duration / 1000.0)
                            );

                    if (frequency < 250) {
                        frequency = 250;
                    }

                    // Fade out
                    double fade =
                            1.0
                                    - (
                                    (double) i
                                            / buffer.length
                            );

                    double wave =
                            Math.sin(
                                    2
                                            * Math.PI
                                            * frequency
                                            * time
                            );

                    buffer[i] =
                            (byte) (
                                    wave
                                            * volume
                                            * fade
                            );
                }

                AudioFormat format =
                        new AudioFormat(
                                sampleRate,
                                8,
                                1,
                                true,
                                false
                        );

                SourceDataLine line =
                        AudioSystem
                                .getSourceDataLine(
                                        format
                                );

                line.open(format);

                line.start();

                line.write(
                        buffer,
                        0,
                        buffer.length
                );

                line.drain();

                line.stop();

                line.close();

            } catch (Exception e) {

                System.out.println(
                        "No se pudo reproducir el sonido."
                );
            }
        }).start();
    }
}