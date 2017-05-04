import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import javax.xml.ws.soap.MTOM;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by sensetime on 17-4-24.
 */
public class Plot extends ApplicationFrame{
    public Plot(String title) {
        super(title);

        final XYSeries series = new XYSeries("Plot Data");

        int L = 15;
        int N = L * L;

        int[] lattice = new int[N];

        for (int i = 0; i < N; i++) {
            lattice[i] = i;
        }

        int n_spins_to_flip = N * 200;

        double[] list_T = new double[13];

        for (int i = 0; i < 13; i++) {
            list_T[i] = 1.0 + 2.0 * i;
        }

        List<Double> list_av_m = new ArrayList<Double>();

        int[] S = new int[N];

        List<Integer> choice = new ArrayList<Integer>();
        choice.add(1);
        choice.add(-1);

        int M = 0;

        for (int i = 0; i < N; i++) {
            S[i] = choice.get(new Random().nextInt(choice.size()));
            M += S[i];
        }

        for (int i = 0; i < list_T.length; i++) {
            double T = list_T[i];

            double M_tot = 0.0;
            int n_flipped_spins = 0;
            int nsteps = 0;

            while (n_flipped_spins < n_spins_to_flip) {
                int k = ThreadLocalRandom.current().nextInt(0, N);

                List<Integer> Pocket = new ArrayList<Integer>();
                List<Integer> Cluster = new ArrayList<Integer>();
                Pocket.add(k);
                Cluster.add(k);

                while (!Pocket.isEmpty()) {
                    int j = Pocket.get(new Random().nextInt(Pocket.size()));
                    double j_h = (int)((j - 1) / L) + 1.0;
                    double j_l = j - L * (int)((j - 1) / L);

                    for (int n = 0; n < lattice.length; n++) {
                        int l = lattice[n];

                        if (l != j) {
                            double l_h = (int)((l - 1) / L) + 1.0;
                            double l_l = l - L * (int)((l - 1) / L);

                            double r2 = Math.pow((l_h - j_h) * (l_h - j_h) + (l_l - j_l) * (l_l - j_l), 0.5);

                            double p = 1.0 - Math.exp(-2.0 / (r2 * T));

                            if (S[l] == S[j] && l != j && !Cluster.contains(l) && new Random().nextDouble() < p) {
                                Pocket.add(l);
                                Cluster.add(l);
                            }
                        }
                    }
                    Pocket.remove(Pocket.indexOf(j));
                }

                for (int m = 0; m < Cluster.size(); m++) {
                    int j = Cluster.get(m);
                    S[j] *= -1;
                }

                M -= 2 * Cluster.size() * S[k];
                n_flipped_spins += Cluster.size();
                M_tot += Math.abs(M);
                nsteps++;
            }

            double av_m= M_tot / (double)nsteps / N;
            list_av_m.add(av_m);

            System.out.println(T);
            System.out.println(av_m);
        }

        for (int i = 0; i < list_T.length; i++) {
            series.add(list_T[i], list_av_m.get(i));
        }

        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                L + " x " + L + "lattice (periodic boundary conditions)",
                "T",
                "<|M|>/N",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800,450));
        setContentPane(chartPanel);
    }
}
