from peewee import MySQLDatabase

db = MySQLDatabase(
    database='IDM',
    user='user',
    password='pass',
    host='0.0.0.0',
    port=3305
)