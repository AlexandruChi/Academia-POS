from peewee import MySQLDatabase

# noinspection SpellCheckingInspection
db = MySQLDatabase(
    database='IDM',
    user='user',
    password='pass',
    host='idmdb',
    port=3306
)