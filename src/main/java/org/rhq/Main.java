package org.rhq;

import java.util.Scanner;

/**
 * Created by jkremser on 12/4/14.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Choose the demo:");
        System.out.println("1     SimpleGraphDemo - creating simple graph, saving it to C*, printing all the vertices and outgoing edges, deleting the graph");
        System.out.println("2     RandomGraphDemo - creating random graph (Erdős–Rényi model) with 10 000 vertices");
        System.out.println("3     RandomTreeDemo  - creating random tree with 10 000 vertices, the sooner the node is created, the more children it has");
        System.out.println("---------------");
        System.out.print("Your option: ");

        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();
        try {
            switch (option) {
                case 1:
                    SimpleGraphDemo.main(args);
                    break;
                case 2:
                    new RandomGraphDemo().run(args, true);
                case 3:
                    new RandomTreeDemo().run(args, true);
                    break;
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            System.err.println("\n\nMake sure the C* backend is started.");
        }
    }
}
