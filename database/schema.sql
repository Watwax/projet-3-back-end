CREATE TABLE `USERS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp,
  `updated_at` timestamp,
  UNIQUE KEY `USERS_index` (`email`)
);

CREATE TABLE `RENTALS` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `surface` integer NOT NULL,
  `price` integer NOT NULL,
  `picture` varchar(255) NOT NULL,
  `description` varchar(2000) NOT NULL,
  `owner_id` integer NOT NULL,
  `created_at` timestamp,
  `updated_at` timestamp,
  CONSTRAINT `FK_RENTALS_OWNER` FOREIGN KEY (`owner_id`) REFERENCES `USERS` (`id`)
);

CREATE TABLE `MESSAGES` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `rental_id` integer,
  `user_id` integer,
  `message` varchar(2000),
  `created_at` timestamp,
  `updated_at` timestamp,
  CONSTRAINT `FK_MESSAGES_USER` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`),
  CONSTRAINT `FK_MESSAGES_RENTAL` FOREIGN KEY (`rental_id`) REFERENCES `RENTALS` (`id`)
);
