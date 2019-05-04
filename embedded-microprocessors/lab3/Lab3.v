`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date:    17:42:13 04/03/2019 
// Design Name: 
// Module Name:    Lab3 
// Project Name: 
// Target Devices: 
// Tool versions: 
// Description: 
//
// Dependencies: 
//
// Revision: 
// Revision 0.01 - File Created
// Additional Comments: 
//
//////////////////////////////////////////////////////////////////////////////////
module Lab3( 	output reg [7:0] out, 
					input wire [7:0] sw, 
					output reg [3:0] an,
					input clk
				);
	 
//---wire declarations---
	wire	[11:0]	address;
	wire	[17:0]	instruction;
	wire			bram_enable;
	wire	[7:0]		port_id;
	wire	[7:0]		out_port;
	reg	[7:0]		in_port;
	wire			write_strobe;
	wire			k_write_strobe;
	wire			read_strobe;
	wire			interrupt;            
	wire			interrupt_ack;
	wire			kcpsm6_sleep;         
	wire			kcpsm6_reset;

//---module declarations---
  kcpsm6 #(
	.interrupt_vector	(12'h3FF),
	.scratch_pad_memory_size(64),
	.hwbuild		(8'h00))
  processor (
	.address 		(address),
	.instruction 	(instruction),
	.bram_enable 	(bram_enable),
	.port_id 		(port_id),
	.write_strobe 	(write_strobe),
	.k_write_strobe 	(k_write_strobe),
	.out_port 		(out_port),
	.read_strobe 	(read_strobe),
	.in_port 		(in_port),
	.interrupt 		(interrupt),
	.interrupt_ack 	(interrupt_ack),
	.reset 		(kcpsm6_reset),
	.sleep		(kcpsm6_sleep),
	.clk 			(clk)); 

// Interrupt and Sleep are not used so tie them to Low
  assign kcpsm6_sleep = 1'b0;
  assign interrupt = 1'b0;

  LEDprogram #(
	.C_FAMILY		   ("S6"),   	//Family 'S6' or 'V6'
	.C_RAM_SIZE_KWORDS	(1),  	//Program size '1', '2' or '4'
	.C_JTAG_LOADER_ENABLE	(1))  	//Include JTAG Loader when set to '1' 
  program_rom (    				//Name to match your PSM file
 	.rdl 			(kcpsm6_reset),
	.enable 		(bram_enable),
	.address 		(address),
	.instruction 	(instruction),
	.clk 			(clk));

//---assigning output of the PicoBlaze and Program ROM to top level module---
  always @ (posedge clk)
  begin

  //---INPUTS---
        case (port_id[1:0]) 
      
        // Read SW at port address 00 hex
        2'b00 : in_port <= sw;

        // Read input_port_b at port address 01 hex
//        2'b01 : in_port <= input_port_b;
//
//        // Read input_port_c at port address 02 hex
//        2'b10 : in_port <= input_port_c;
//
//        // Read input_port_d at port address 03 hex
//        2'b11 : in_port <= input_port_d;

        // To ensure minimum logic implementation when defining a multiplexer always
        // use don't care for any of the unused cases (although there are none in this 
        // example).

        default : in_port <= 8'bXXXXXXXX ;  

      endcase
  
	//---OUTPUTS---
      // 'write_strobe' is used to qualify all writes to general output ports.
      if (write_strobe == 1'b1) begin
		
        // Write to out at port address 01 hex
//        if (port_id[0] == 1'b1) begin
//          out <= out_port;
//        end
		  
		 // Write to out at port address 02 hex
//        if (port_id[1] == 1'b1) begin
//          an <= out_port[3:0];
//        end
		  
		 // Write to out at port address 08 hex
        if (port_id[3] == 1'b1) begin
          an <= out_port[3:0];
        end
		  
		 // Write to out at port address 10 hex (16 decimal)
        if (port_id[4] == 1'b1) begin
          out <= out_port;
        end
		  
      end
  end

endmodule
