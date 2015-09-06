# An SMSC Stub Server in Akka

This project implements a partial Short Message Service Centre (SMSC) stub server using [Akka](http://akka.io/) and [Spray](http://spray.io/).

It implements some of the SMSC side of the [Short Message Peer to Peer Protocol Specification v3.4](http://opensmpp.org/specs/SMPP_v3_4_Issue1_2.pdf).

## What is it for?

This stub is useful for testing software that runs as an SMPP External Short Message Entity (ESME), especially performance testing. It has been used in a Telco for performance testing of a service that provides premium services over SMS.

## How does it work?

When running the server sends and receives SMPP messages, writing log entries for each one. There are no records of the messages held in memory or other persistent storage. 

### Receiving

It accepts SMPP messages from ESMEs, replying immediately with successful response messages:

- `bind_transmitter` -> `bind_transmitter_resp`
- `bind_receiver` -> `bind_receiver_resp`
- `bind_transceiver` -> `bind_transceiver_resp`
- `unbind` -> `unbind_resp`
- `enquire_link` -> `enquire_link_resp`
- `submit_sm` (without optional parameters) -> `submit_sm_resp`
- `cancel_sm` -> `cancel_sm_resp`
- `generic_nack` (no response message)

This functionality is used to test mobile-terminated (MT) SMS messages.

### Sending

It has an HTTP control interface that can be used to:

- send `deliver_sm` (without optional parameters) to a current ESME `bind_receiver` or `bind_transceiver` connection, used to test mobile-originated (MO) SMS messages
- shutdown the stub server

The control interface is implemented using [Spray](http://spray.io/).

## How to use it

This is an SBT project currently using:

- Scala 2.11.7
- SBT 0.13.8
- Akka 2.3.12
- Spray 1.3.3

Start the server using `sbt run`. Stop a running server by sending an HTTP GET request to `/shutdown`, e.g.:

    curl http://localhost:18080/shutdown
    
The SBT project also includes the [sbt-onejar](https://github.com/sbt/sbt-onejar) plugin. Run `sbt one-jar` to create a single über-JAR that can be run using `java -jar <JAR name>`.

Once running it will accept binds and messages from ESMEs on the configured port (see below).

### Sending MO SMS

The HTTP interface has the URI `/send` to send an MO SMS with parameters:

- `from`: the sender address
- `to`: the recipient address
- `msg`: the text of the message

For example:

    curl "http://localhost:18080/send?from=61401001001&to=176&msg=Test%20message"

When sending MO messages the specified sender and recipient address strings are matched as described below for construction of the in the `deliver_sm` PDUs.
 
### Address types

SMPP addresses are categorised by Type of Number (TON) and Numbering Plan Indicator (NPI). The server matches these combinations from an address string:
 
- Australian MSISDNs of the form 614xxxxxxxx (TON: International, NPI: ISDN)
- International MSISDNs of the form +xxxxxxxxxx (TON: International, NPI: ISDN)
- Short codes like 176 or 1234 (TON: National, NPI: ISDN)
- Alphabetic like `Skip` or `Telstra` (TON: Alphanumeric, NPI: Unknown)

Unmatched addresses resolve to TON: Unknown, NPI: Unknown.

## Configuration

The server uses the [Typesafe Configuration Library](https://github.com/typesafehub/config). The embedded `application.conf` sets these values:

- SMPP listening port: 10300
- HTTP listening port: 18080

These values can be overridden with environment variables `SMPP_PORT` and `HTTP_PORT`, respectively.

## Logging

Logging is implmented using [Logback](http://logback.qos.ch/). The default configuration writes entries at INFO level into two log files in the `logs` directory:

- `smsc-stub.log`: general logging about messages sent and received, including binds and enquiries
- `end-to-end.log`: CSV entries of `submit_sm` messages received and `deliver_sm` messages sent with timestamps and message bodies.

## Limitations

The server does not accept optional parameters in `submit_sm` PDUs, nor accept them in `deliver_sm` PDUs it sends. 

Similarly, it also only accepts complete messages contents in the `short_message` parameter and does not manage concatenated message parts.

It does not recognise these messages from an ESME:
 
- `submit_sm_multi` -> `submit_sm_multi_resp`
- `replace_sm` -> `replace_sm_resp`
- `data_sm` -> `data_sm_resp`
- `query_sm` -> `query_sm_resp`
- `alert_notification`

It cannot send these messages to an ESME:

- `enquire_link` -> `enquire_link_resp`
- `unbind` -> `unbind_resp`
- `data_sm` -> `data_sm_resp`
- `generic_nack`
- `alert_notification`

WARNING: Some of the Scala code and organisation of the project is not very sophisticated. I am new to Scala and am learning my way. Please be nice to me!
