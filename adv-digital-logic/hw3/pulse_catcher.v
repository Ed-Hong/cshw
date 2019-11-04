`timescale 1ns / 1ps

module pulse_catcher(
    input P,
    input R,
    output Z
    );
	 
	 wire #20 Pskew = P ;
	 
	 not  #5 U8 (RNOT, R ) ;
	 not  #5 I9 (y1NOT, y1 ) ;
	
	 and  #5 U1 (net1, P, R ) ;
	 and  #5 U2 (net2, P, y1 ) ;
	 and  #5 U3 (net3, Z, RNOT ) ;
	 and  #5 U4 (net4, y1NOT, Z, Pskew ) ;
	 and  #5 U5 (net5, y1NOT, Pskew, RNOT ) ;
	
	 or   #5 U6 (y1, net1, net2 ) ;
	 or   #5 U7 (Z, net3, net4, net5 ) ;

endmodule
