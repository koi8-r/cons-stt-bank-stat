package ru.net.oz.cons.stt

/*
    Structure:
    +---------------------------+
    | S | T | T | ? | ? | ? | ? | ; 7 byte header
    +---------------------------+
    |          zlib data        |
    +---------------------------+

    Uncompress zlib in cli:
    $ cat file.stt | dd bs=1 skip=7 | openssl zlib -d

    Info banks:
    https://www.consultant.ru/about/software/systems/

    Win cmd unicode:
    chcp 65001
 */

import java.util.zip.InflaterInputStream

def banks = [
    'BCN'    : 'ФАС Центрального округа (бухгалтер)',
    'BDV'    : 'ФАС Дальневосточного округа (бухгалтер)',
    'BMS'    : 'ФАС Московского округа (бухгалтер)',
    'BPV'    : 'ФАС Поволжского округа (бухгалтер)',
    'BRB'    : 'СудебнаяПрактика - решения высших судов (бухгалтер)',
    'BSK'    : 'ФАС Северо-Кавказского округа (бухгалтер)',
    'BSZ'    : 'ФАС Северо-Западного округа (бухгалтер)',
    'BUR'    : 'ФАС Уральского округа (бухгалтер)',
    'BVS'    : 'ФАС Восточно-Сибирского округа (бухгалтер)',
    'BVV'    : 'ФАС Волго-Вятского округа (бухгалтер)',
    'BZS'    : 'ФАС Западно-Сибирского округа (бухгалтер)',
    'CJI'    : 'Юридическая пресса',
    'CMB'    : 'Постатейные комментарии и книги',
    'LAW'    : 'Версия Проф',
    'PAP'    : 'Деловые бумаги',
    'PBI'    : 'Бухгалтерская пресса и книги',
    'PDR'    : 'Путеводитель по договорной работе', //?
    'PKS'    : 'Путеводитель по корпоративным спорам',
    'PKV'    : 'Путеводитель по кадровым вопросам', //?
    'DOF'    : 'Дополнительные формы', //?
    'PPN'    : 'Путеводитель по налогам',
    'PPS'    : 'Путеводитель по сделкам',
    'PSP'    : 'Путеводитель по судебной практике (ГК РФ)',
    'QSA'    : 'Вопросы ответы (в составе КБ)',
    'RLAW087': 'Мурманская область',
    'PPVS'   : 'Правовые позиции высших судов', //?
    'PKP'    : 'Путеводитель по корпоративным процедурам',
    'PGU'    : 'Путеводитель по госуслугам для юридических лиц', //?
    'KOR'    : 'Корреспонденция Счетов',
    'PTS'    : 'Путеводитель по трудовым спорам',
    'RGSS'   : 'Решения госорганов по спорным ситуациям', //?
    'PSG'    : 'Путеводитель по спорам в сфере закупок', //?
    'PKG'    : 'Путеводитель по контрактной системе в сфере закупок', //?
    'ARB'    : 'Судебная практика - решения высших судов',
    'SIP'    : 'Суд по интеллектуальным правам' // ?
]

result = [:] as Map<String, Integer>

['d:/STS1/', 'd:/STS2/'].each {

    fp ->
        new File( fp ).listFiles().findAll {
            it.file && it.name ==~ /(?i).*\.STT$/
        }.each {
            f ->
                f.withDataInputStream {
                    DataInputStream dis ->
                        def buf = new byte[7]
                        dis.readFully(buf, 0, buf.length)

                        if (
                        buf[0..2] == "STT".bytes as List<Byte>
                        // TODO: buf[3..4] == CRC() ?
                        ) {
                            new InflaterInputStream(dis).eachLine {
                                m = it =~ /(?i)^DOC_OPEN_IM\s+([^\s]+).*/
                                if (m.matches()) {
                                    def key = m.group(1)
                                    if (result.containsKey(key))
                                        result[key]++
                                    else
                                        result[key] = 1
                                }
                                null
                            }
                        } else {
                            throw new IOException("'${f.name}' not have a STT header")
                        }
                }
        }
}



result.sort { a, b -> b.value <=> a.value } each {
    k, v ->
        printf("%04d times: '${banks[k]}'\n", v)
}
