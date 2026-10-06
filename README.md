# CloudStream Extensions

CloudStream eklentisi geliştirmek ve kendi eklenti deponu kurmak için başlangıç şablonu. Örnek modül: `ExampleProvider`.

## Depoyu CloudStream’e ekleme

CloudStream’de **Ayarlar → Eklentiler → Depo ekle** bölümüne şu adresi girin:

```text
https://raw.githubusercontent.com/Yek5353/cloudstream-extensions/main/repo.json
```

Depo listesi `builds` dalındaki `plugins.json` dosyasını okur. Henüz yayımlanmış eklenti yok.

## Eklenti geliştirme

1. `ExampleProvider` klasörünü yeni eklenti için kopyalayıp klasör adını değiştirin.
2. `ExamplePlugin.kt` içinde eklentiyi kaydedin; `ExampleProvider.kt` içinde sağlayıcı adını, site adresini, dili ve arama davranışını düzenleyin.
3. Modülün `build.gradle.kts` dosyasında açıklama, yazar, tür, dil ve sürüm bilgisini ayarlayın.
4. Bilgisayarınızda JDK 17, Android SDK ve Gradle kurulu olmalı. Proje kökünde `gradle make makePluginsJson` komutuyla derleyin.
5. GitHub’daki **Actions → Build CloudStream plugins** akışı da her push’ta derleme yapar. Oluşan `.cs3` paketleri ve `plugins.json` dosyası **Artifacts** bölümünden indirilebilir.

GitHub Actions şu anda derleme çıktısını artifact olarak saklar. CloudStream’de depodan kurulabilir hale getirmek için `.cs3` paketlerini `builds` dalına koyup `plugins.json` listesini güncellemek gerekir. GitHub Actions için yazma izni açılmadı.

## Kaynaklar

- [CloudStream eklenti şablonunu kullanma](https://recloudstream.github.io/csdocs/devs/using-plugin-template/)
- [JSON repo manifesti oluşturma](https://recloudstream.github.io/csdocs/devs/create-your-own-json-repository/)
- [Resmî TestPlugins şablonu](https://github.com/recloudstream/TestPlugins)
# CloudStream Extensions

CloudStream eklenti deposu başlangıç noktası.

## Depoyu CloudStream'e ekleme

Repo GitHub'a yüklendikten sonra CloudStream içindeki **Ayarlar → Eklentiler → Depo ekle** bölümüne şu adresi girin:

```text
https://raw.githubusercontent.com/Yek5353/cloudstream-extensions/main/repo.json
```

Henüz eklenti eklenmedi. Eklenti kaynakları eklendiğinde CloudStream Gradle eklenti şablonuyla derlenip `builds/plugins.json` listesine yayımlanmalıdır.

## Eklenti geliştirme

CloudStream'in resmî [TestPlugins şablonunu](https://github.com/recloudstream/TestPlugins) temel alın. Yeni bir eklenti modülü ve GitHub Actions yayın akışı eklendiğinde `builds/plugins.json` listesine eklentiler yayımlanabilir.

## Kaynaklar

- [CloudStream eklenti şablonunu kullanma](https://recloudstream.github.io/csdocs/devs/using-plugin-template/)
- [JSON repo manifesti oluşturma](https://recloudstream.github.io/csdocs/devs/create-your-own-json-repository/)
