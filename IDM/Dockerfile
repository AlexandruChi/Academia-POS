FROM python:3.13

COPY . .

RUN pip install -r requirements.txt
RUN sh compileProto.sh

CMD python IDM.py