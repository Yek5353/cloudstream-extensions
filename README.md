# Yek5353 CloudStream TR

Yek5353 hesabında yayımlanan Türkçe CloudStream eklenti deposu. Bu depo, [Emre-Kahveci/CloudStreamHub](https://github.com/Emre-Kahveci/CloudStreamHub) projesinden uyarlanmıştır; sağlayıcıların özgün yazar bilgileri ve GPL-3.0 lisans bildirimi korunur.

## CloudStream’e ekleme

CloudStream’de **Ayarlar → Eklentiler → Depo ekle** bölümüne şu adresi girin:

```text
https://raw.githubusercontent.com/Yek5353/cloudstream-extensions/main/repo.json
```

Depo, yayınlanan eklenti paketlerini `builds` dalındaki `plugins.json` üzerinden sunar. `main` dalına gönderilen değişiklikler GitHub Actions ile derlenir ve başarılı derlemeden sonra paketler yayımlanır.

## Etkin sağlayıcılar

| Eklenti | Modül | İçerik türleri |
| --- | --- | --- |
| AnimeciX | `AnimeciX` | Anime |
| BelgeselX | `BelgeselX` | Belgesel |
| Film Makinesi | `FilmMakinesi` | Film, dizi |
| 4KHDHub | `FourKHDHub` | Film |
| HDFilmCehennemi | `HDFilmCehennemi` | Film, dizi |
| Kült Filmler | `KultFilmler` | Film, dizi |
| Yeşilçam TV | `YesilCamTv` | Film |
| Sezonluk Dizi | `SezonlukDizi` | Dizi |
| SinemaCX | `SinemaCX` | Film |
| DiziYou | `DiziYou` | Dizi |
| Dizilla | `Dizilla` | Dizi |
| FullHDFilmizlesene | `FullHDFilmizlesene` | Film |
| JetFilmİzle | `JetFilmIzle` | Film |
| CloudStreamHub | `CloudStreamHub` | Film, dizi, anime, çizgi dizi, belgesel |
| DiziKorea | `DiziKorea` | Asya dizisi |
| DramaDizilerim | `DramaDizilerim` | Asya dizisi |
| WebDramaTurkey | `WebDramaTurkey` | Asya dizisi |
| Animeler | `Animeler` | Anime, anime filmi |
| SetFilmİzle | `SetFilmIzle` | Film, dizi |
| HDFilmDelisi | `HDFilmDelisi` | Film |
| Dizigecesi | `Dizigecesi` | Dizi, film |
| RareFilmm | `RareFilmm` | Film |
| YTS | `YTS` | Torrent, film |

Etkinlik ve kaynak sağlığı zamanla değişebilir. Devre dışı sağlayıcılar `config/providers.json` içinde işaretlenir ve CloudStream kataloğuna yayımlanmaz.

## Derleme

Geliştirme için JDK 17 ve Android SDK gerekir. Depo kökünde:

```bash
./gradlew make makePluginsJson
```

Depo yapılandırmasını kontrol etmek için:

```bash
python tools/validate_repo.py
```

## Kaynak ve lisans

- Kaynak proje: [Emre-Kahveci/CloudStreamHub](https://github.com/Emre-Kahveci/CloudStreamHub)
- Eklenti şablonu: [CloudStream geliştirici kılavuzu](https://recloudstream.github.io/csdocs/devs/using-plugin-template/)
- Lisans: [GNU GPL v3.0](LICENSE)

Bu depo medya dosyası barındırmaz. Sağlayıcılar herkese açık web sayfalarındaki katalog ve bağlantı bilgilerini CloudStream uygulamasında kullanmak üzere okur.
