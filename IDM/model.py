from peewee import Model, IntegerField, CharField, DateTimeField
from database import db

class IDMModel(Model):
    class Meta:
        database = db

class User(IDMModel):
    ID = IntegerField(column_name='ID', primary_key=True)
    email = CharField(column_name='email')
    password = CharField(column_name='password')
    role = CharField(column_name='role')

    class Meta:
        db_table = 'IDM'

class InvalidToken(IDMModel):
    UUID = CharField(column_name='UUID', primary_key=True)
    expiration = DateTimeField(column_name='expiration')

    class Meta:
        db_table = 'Invalid'