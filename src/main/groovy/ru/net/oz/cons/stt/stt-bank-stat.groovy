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
    'LAW':     'Российское законодательство',
    'RLAW087': 'Мурманская область',
    'PBI':     'Бухгалтерская пресса и книги',
    'PAP':     'Деловые бумаги',
    'QSA':     'Вопросы ответы',
    'PPN':     'Путеводитель по налогам',
    'PKV':     'Путеводитель по кадровым вопросам',
    'CMB':     'Постатейные комментарии и книги',
    'PPS':     'Путеводитель по сделкам',
    'CJI':     'Юридическая пресса',
    'KOR':     'Корреспонденция Счетов',
    'ARB':     'Решения высших судов',
    'PSP':     'Путеводитель по судебной практике (ГК РФ)',
    'PTS':     'Путеводитель по трудовым спорам',
    'PKS':     'Путеводитель по корпоративным спорам'
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
        printf("%04d times: $k - '${banks[k]}'\n", v)
}
