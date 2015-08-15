# smsc-stub: an SMSC Stub Server in Akka

This project implements a partial Short Message Service Centre (SMSC) stub server using the Akka framework.

It implements the SMSC side of the Short Message Peer to Peer Protocol (SMPP) version 3.4.

## Currently implemented

The following PDU transactions are implmented:
- `bind_transmitter` -> `bind_transmitter_resp`

## To do

Not necessarily in this order!

ESME -> SMSC PDU transactions:
- `bind_receiver` -> `bind_receiver_resp`
- `bind_transceiver` -> `bind_transceiver_resp`
- `unbind` -> `unbind_resp`
- `enquire_link` -> `enquire_link_resp`
- `submit_sm` -> `submit_sm_resp`
- `submit_sm_multi` -> `submit_sm_multi_resp`
- `replace_sm` -> `replace_sm_resp`
- `data_sm` -> `data_sm_resp`
- `query_sm` -> `query_sm_resp`
- `cancel_sm` -> `cancel_sm_resp`
- `generic_nack`
- `alert_notification`

SMSC -> ESME:
- `enquire_link` -> `enquire_link_resp`
- `unbind` -> `unbind_resp`
- `deliver_sm` -> `deliver_sm_resp`
- `generic_nack`
- `alert_notification`
