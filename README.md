<h1 align="center">Gorzdrav Telegram Bot</h1>
<div align="center">
    <img src="https://img.shields.io/badge/Java-black?style=for-the-badge&logo=Java" alt="Java"/>
    <img src="https://img.shields.io/badge/Spring-black?style=for-the-badge&logo=Spring" alt="Spring"/>
    <img src="https://img.shields.io/badge/Postgres-black?style=for-the-badge" alt="Postgres"/>
    <img src="https://github.com/coollappsus/gorzdrav_spb_bot/actions/workflows/maven.yml/badge.svg" alt="Status"/>
</div>

<h3></h3>
<h1>Описание</h1>
Проект создавался в учебных целях "Для себя" с последующим развитием.
Представляет из себя телеграм бота, в котором реализован функционал api горздрава + полезные функции.

<h1>Кратко о проекте</h1>
Основной целью было обучение, чтобы в последствии получить работающее приложение, где фронтом будет клиент телеграма.
В проекте написан собственный CI\CD, благодаря которому при каждом принятом pull request или push в main ветку происходит 
сборка проекта, прогон тестов и деплой на удаленный сервер, что сильно упрощает дальнейшую разработку. Разворачиваемся 
в docker контейнерах.

<h1>Основной функционал</h1>
<ul>
    <li>Создание карточки пациента. У каждого пользователя свои карточки пациентов</li>
    <li>Запись к врачу</li>
    <li>Просмотр существующих записей</li>
    <li>Отмена записей к врачу</li>
    <li>Отслеживание талончиков к определенному врачу с последующей запись и информированием пользователя о новой записи</li>
</ul>

# API example request
https://gorzdrav.spb.ru/_api/api/v2/shared/districts - список районов
https://gorzdrav.spb.ru/_api/api/v2/shared/district/10/lpus - список медучреждений в 10 районе
https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/229/specialties - информация по всем свободным специальностям в больнице с ид 229
https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/30/speciality/981/doctors - информация по доступным врачам в больнице 30 по специальности 981
https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/1138/doctor/36/timetable - расписание врача 36 в больнице 1138
https://gorzdrav.spb.ru/_api/api/v2/schedule/lpu/30/doctor/222618/appointments - доступные назначения к врачу

# Gorzdrav API
https://github.com/egorantonov/gorzdrav/wiki/SPB-Gorzdrav-API-Documentation
