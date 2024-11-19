/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2023 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.dto;

import io.swagger.annotations.ApiModelProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 31.08.2015
 */
@Getter
@Setter
@ToString(callSuper = true)
public class TransactionStopForm {
    // Internal database Id
    @ApiModelProperty(value = "Database primary key of the transaction")
    private Integer transactionId;

    // Charge Point ID
    @ApiModelProperty(value = "Charge Point ID")
    private String chargePointId;

    public Integer getTransactionId() {
        return this.transactionId;
    }
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }
    public String getChargePointId() {
        return this.chargePointId;
    }
    public void setChargePointId(String chargePointId) {
        this.chargePointId = chargePointId;
    }
}
