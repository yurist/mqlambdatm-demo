# Overview

Sample implementation of IBM MQ message processing using Lambda triggering. The sample expects input messages in JSON format, representing related records `order` and `orderdetails`. It parses the messages using Google `gson` parser and inserts the records into a MySQL database. The logic is idempotent - it is assumed that order number is unique, and an input message replaces the corresponding records in the database if they exist.

The sample is based on `generic-message-handler`, therefore you will not find any queue manipulation logic here - it is all in the parent class.
