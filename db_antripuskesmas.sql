-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Dec 23, 2025 at 12:35 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_antripuskesmas`
--

-- --------------------------------------------------------

--
-- Table structure for table `antrian_pasien`
--

CREATE TABLE `antrian_pasien` (
  `id_antrian` int(11) NOT NULL,
  `no_antrian` int(11) NOT NULL,
  `nama` varchar(150) NOT NULL,
  `umur` int(11) NOT NULL,
  `jenis_kelamin` enum('L','P') NOT NULL,
  `tinggi_cm` int(11) DEFAULT NULL,
  `berat_kg` int(11) DEFAULT NULL,
  `gejala` text DEFAULT NULL,
  `dokter_pilihan` varchar(150) DEFAULT NULL,
  `status` enum('Registered','Waiting','Called','InConsultation','Finished','Cancelled') DEFAULT 'Registered',
  `waktu_daftar` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `antrian_pasien`
--

INSERT INTO `antrian_pasien` (`id_antrian`, `no_antrian`, `nama`, `umur`, `jenis_kelamin`, `tinggi_cm`, `berat_kg`, `gejala`, `dokter_pilihan`, `status`, `waktu_daftar`) VALUES
(6, 1, 'Meindra', 19, 'L', 170, 80, 'PMO Disorder', 'Dr. Konz Ganz', 'Called', '2025-12-04 15:29:58'),
(7, 2, 'Satria', 80, 'L', 200, 100, 'Rasis sama orang hitam', 'Dr. Konz Ganz', 'Called', '2025-12-04 15:30:29'),
(8, 3, 'Robert', 19, 'L', 178, 50, 'Stress', 'Drs. Memz Canz', 'Called', '2025-12-04 16:31:26'),
(9, 4, 'Ivanko', 738, 'P', 232, 23, 'hi', 'Drs. Memz Canz', 'Called', '2025-12-04 16:34:21');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `antrian_pasien`
--
ALTER TABLE `antrian_pasien`
  ADD PRIMARY KEY (`id_antrian`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `antrian_pasien`
--
ALTER TABLE `antrian_pasien`
  MODIFY `id_antrian` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
