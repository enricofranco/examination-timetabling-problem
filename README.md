# Examination timetabling problem
This repository contains a solver for the well known **examination timetabling problem**.
In this simplified implementation a university scenario is emulated: a list of exams, a list of enrolled students and a number of available timeslots on which assign exams are given.
The goal is to find a solution which must avoid any type of conflict due to overlapping exams for each student and tries minimize a penalty function which depends on the mutual distances of the exams which may create conflicts for each students.
The repository contains
- the mathematical model of the problem (in LaTeX);
- the final report with results and a full explanation of the algorithm and the code (in LaTeX);
- the entire code implementation (in Java).

The strategy implemented does not exploit a standard heuristic or meta-heuristic algorithm. Insted, our algorithm adopts a mixed approach which exploits a taboo list paradigm with the concept of random mutation to escape from flat regions.
Contributors, in alphabetic order:
- @bubazzo
- @franzonistefano
- @Vooriden
- @CapCandy