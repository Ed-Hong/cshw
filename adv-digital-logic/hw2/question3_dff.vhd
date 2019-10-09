library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.ALL;
use ieee.std_logic_unsigned.all;

entity question3_dff is
    port (
        q   :out std_logic;
        d   :in  std_logic;
        clk :in  std_logic;
        rst :in  std_logic;
   );
end question3_dff;

architecture dff_behav of question3_dff is
begin
    process(clk,rst) begin
        if(rst = '1') then
            q <= '0';
        elsif(falling_edge(clk)) then
            q <= d;
        end if;     
    end process; 
end dff_behav;