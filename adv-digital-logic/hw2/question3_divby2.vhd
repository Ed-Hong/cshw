library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.ALL;
use ieee.std_logic_unsigned.all;

entity question3_divby2 is
    port (
        f   :out std_logic;
        clk :in  std_logic;
        rst :in  std_logic
   );
end question3_divby2;

architecture divby2_behav of question3_divby2 is
    component question3_dff is
        port (
            q   :out std_logic;
            d   :in  std_logic;
            clk :in  std_logic;
            rst :in  std_logic
        );
    end question3_dff;
    signal qnot: std_logic;
begin
    dff: question3_dff port map (
        clk => clk,
        rst => rst,
        d => qnot,
        q => f
    );

    process(clk,rst) begin
        if(rst = '1') then
            qnot <= '0';
        elsif(falling_edge(clk)) then
            qnot <= not q;
        end if; 
    end process;
    
end divby2_behav;