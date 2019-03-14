/**
 * Author: Edward Hong
 * CE 4337.003
 * March 13 2019
 */

/* Function 1: check if an integer is an odd multiple of 3 */
/* First clause checks if n is an integer, otherwise fail  */
oddMultOf3(Num) :- 
    \+ integer(Num),
    !,
    print("ERROR: The given parameter is not an integer"),
    fail.
/* Second clause checks if n is odd */
oddMultOf3(Num) :- 1 is n mod 2.
/* Third clause checks if n is multiple of 3 */
oddMultOf3(Num) :- 0 is n mod 3.

/* Function 2: Compute product of all numbers in the list (first param) and saves to second param */
list_prod(List,Num).
/* Empty list has product 0 */
list_prod([],0).

list_prod([H],H).
list_prod([H|T], Product) :- 
    list_prod(T, Tail), 
    Product is Tail * H.

/* Function 3: Segregates a given list into the even and odd values */
/* If given list is empty */
segregate([],[],[]).
/* Clause for the even numbers */
segregate([N|Nums],[N|Even],Odd) :- N mod 2 =:= 0,segregate(Nums,Even,Odd).
/* Clause for the odd numbers */
segregate([N|Nums],Even,[N|Odd]) :- N mod 2 =:= 1,segregate(Nums,Even,Odd).

/* Function 4: Find the routes from city A to city B */
/* Define the graph */
path(seattle,omaha).
path(seattle,dallas).
path(fresno,seattle).
path(fresno,albany).
path(fresno,boston).
path(omaha,albany).
path(omaha,atlanta).
path(albany,seattle).
path(albany,dallas).
path(dallas,seattle).
path(dallas,albany).
path(atlanta,boston).
path(atlanta,dallas).
path(atlanta,albany).

route(A,B,Route) :- direction(A,B,[A],Q), reverse(Q,Route).
direction(A,B,P,[B|P]) :- path(A,B).

/* DFS traversal to find all paths from A to B */
direction(A,B,Visited,Route) :- 
    path(A,C), 
    C \== B, 
    \+member(C,Visited), 
    direction(C,B,[C|Visited],Route).

/* Function 5: Set of predicates to determine Geneaology */
parent(X,Y) :- 
    parent(X,Y).

child(X,Y):- 
    parent(X,Y).

mother(X,Y) :- 
    parent(X,Y), 
    female(X).

father(X,Y):-
    parent(X,Y),
    male(X).

grandparent(X,Y):-
    parent(Z,Y),
    parent(X,Z).

grandfather(X,Y):-
    grandparent(X,Y),
    male(X).

grandmother(X,Y):-
    grandparent(X,Y),
    female(X).

grandchild(X,Y):-
    parent(Y,Z),
    parent(Z,X).

grandson(X,Y):-
    grandchild(X,Y),
    male(X).

granddaughter(X,Y):-
    grandchild(X,Y),
    female(X).

sibling(X,Y):-
    parent(Z,X),
    parent(Z,Y),
    not(X=Y).


















