`timescale 1ns / 1ps

module rail_crossing(
    input x,
    input y,
    input c
    );
	 
	 not  #5 U6 (ynot, y ) ;
	
	 and  #5 U1 (net1, z1, x ) ;
	 and  #5 U2 (net2, x, y, z2 ) ;
	 and  #5 U4 (z2, x, ynot ) ;
	
	 or   #5 U3 (z1, net1, net2 ) ;
	 
	 xor  #5 U5 (c, z1, z2 ) ;

endmodule
