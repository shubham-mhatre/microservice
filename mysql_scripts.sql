
CREATE TABLE `product` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NULL DEFAULT NULL,
	`description` VARCHAR(255) NULL DEFAULT NULL,
	`price` DOUBLE NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
ENGINE=InnoDB
;

CREATE TABLE `orders` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`order_number` VARCHAR(50) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `orders_line_items` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`price` DECIMAL(38,2) NULL DEFAULT NULL,
	`quantity` INT(11) NULL DEFAULT NULL,
	`sku_code` VARCHAR(255) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `orders_order_line_items` (
	`order_id` INT(11) NOT NULL,
	`order_line_items_id` INT(11) NOT NULL,
	UNIQUE INDEX `UKo61x047ux9nbd024nnx3ic8af` (`order_line_items_id`),
	INDEX `FKq49nm4vsqfm7l2eqj953kqa1q` (`order_id`),
	CONSTRAINT `FK9yvmjjttaif8c1oe9tckdugw8` FOREIGN KEY (`order_line_items_id`) REFERENCES `orders_line_items` (`id`),
	CONSTRAINT `FKq49nm4vsqfm7l2eqj953kqa1q` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `inventory` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`quantity` INT(11) NULL DEFAULT NULL,
	`sku_code` VARCHAR(255) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


CREATE TABLE `user_credentials` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NULL,
	`email` VARCHAR(50) NULL,
	`password` VARCHAR(50) NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
