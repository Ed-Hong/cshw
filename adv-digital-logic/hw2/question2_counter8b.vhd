library ieee ;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.ALL;
use ieee.std_logic_unsigned.all;

entity question2_counter8b is
    port (
        c       :out std_logic_vector (7 downto 0); -- 8bit vector which acts as counter output
        clk     :in  std_logic;
        rst     :in  std_logic;
    );
end entity;

architecture counter_behav of question2_counter8b is
    signal count: std_logic_vector (7 downto 0);
begin
    process (clk, rst) begin
        if (rst = '1') then
            count <= (others=>'0');
        elsif (rising_edge(clk)) then
                count <= count + 1;
        end if;
    end process;
    c <= count;
end architecture;