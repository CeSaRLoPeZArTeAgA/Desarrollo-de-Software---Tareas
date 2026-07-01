FROM python:3.12-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app/src

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
RUN mkdir -p data src/pet_alerts/uploads

EXPOSE 8000
CMD ["uvicorn", "pet_alerts.main:app", "--host", "0.0.0.0", "--port", "8000"]
