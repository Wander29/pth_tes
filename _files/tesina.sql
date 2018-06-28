-- phpMyAdmin SQL Dump
-- version 4.7.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Creato il: Giu 27, 2018 alle 18:29
-- Versione del server: 5.7.17
-- Versione PHP: 7.1.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `tesina`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `tipo_utente`
--

CREATE TABLE `tipo_utente` (
  `CodTipoUt` int(5) NOT NULL,
  `nomeTipo` varchar(50) NOT NULL,
  `permessi` varchar(200) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `tipo_utente`
--

INSERT INTO `tipo_utente` (`CodTipoUt`, `nomeTipo`, `permessi`) VALUES
(1, 'utente', 'HOME_VIEW'),
(2, 'ospite', 'HOME_VIEW'),
(3, 'admin', 'HOME_VIEW_INS');

-- --------------------------------------------------------

--
-- Struttura della tabella `users`
--

CREATE TABLE `users` (
  `CodUser` int(5) UNSIGNED NOT NULL,
  `nomeUt` varchar(200) NOT NULL,
  `psw` varchar(32) NOT NULL,
  `FkTipoUtente` int(5) UNSIGNED NOT NULL,
  `FkCodRel` int(5) UNSIGNED NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `users`
--

INSERT INTO `users` (`CodUser`, `nomeUt`, `psw`, `FkTipoUtente`, `FkCodRel`) VALUES
(1, 'Ludo29', '189bbbb00c5f1fb7fba9ad9285f193d1', 1, 1),
(2, 'admin', '21232f297a57a5a743894a0e4a801fc3', 3, 1),
(3, 'utente', '189BBBB00C5F1FB7FBA9AD9285F193D1', 1, 2);

-- --------------------------------------------------------

--
-- Struttura della tabella `utente`
--

CREATE TABLE `utente` (
  `CodUt` int(5) UNSIGNED NOT NULL,
  `nome` varchar(30) NOT NULL,
  `cognome` varchar(30) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `utente`
--

INSERT INTO `utente` (`CodUt`, `nome`, `cognome`) VALUES
(1, 'Ludovico', 'Venturi'),
(2, 'Riccardi', 'Marco');

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `tipo_utente`
--
ALTER TABLE `tipo_utente`
  ADD PRIMARY KEY (`CodTipoUt`);

--
-- Indici per le tabelle `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`CodUser`),
  ADD KEY `FkTipoUtente` (`FkTipoUtente`),
  ADD KEY `FkCodRel` (`FkCodRel`);

--
-- Indici per le tabelle `utente`
--
ALTER TABLE `utente`
  ADD PRIMARY KEY (`CodUt`);

--
-- AUTO_INCREMENT per le tabelle scaricate
--

--
-- AUTO_INCREMENT per la tabella `tipo_utente`
--
ALTER TABLE `tipo_utente`
  MODIFY `CodTipoUt` int(5) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT per la tabella `users`
--
ALTER TABLE `users`
  MODIFY `CodUser` int(5) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT per la tabella `utente`
--
ALTER TABLE `utente`
  MODIFY `CodUt` int(5) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
