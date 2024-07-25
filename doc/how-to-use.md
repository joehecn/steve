https://github.com/steve-community/steve

# api
http://localhost:8180/steve/manager/v3/api-docs
http://10.12.1.37:8180/steve/manager/v3/api-docs

# Update Database
```sql
CREATE TABLE `organization_device` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `charge_box_pk` int(11) NOT NULL,
  `organization_application_id` int(11) NOT NULL,
  `voltage` varchar(16) DEFAULT NULL,
  `current` varchar(16) DEFAULT NULL,
  `power` varchar(16) DEFAULT NULL,

  PRIMARY KEY (`id`),
  CONSTRAINT `FK_charge_box_pk` FOREIGN KEY (`charge_box_pk`)
    REFERENCES `charge_box` (`charge_box_pk`)
    ON DELETE CASCADE ON UPDATE NO ACTION
);
```
