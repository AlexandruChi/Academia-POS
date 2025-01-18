FROM python:3.13

COPY . .

RUN pip install -r requirements.txt
RUN sh compileProto.sh

ENV APP_PORT=8000

CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port $APP_PORT"]