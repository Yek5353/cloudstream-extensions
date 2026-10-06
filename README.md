# CloudStream Extensions

CloudStream eklentisi geliştirmek ve kendi eklenti deponu kurmak için başlangıç şablonu. Örnek modül: `ExampleProvider`.

## Depoyu CloudStream’e ekleme

CloudStream’de **Ayarlar → Eklentiler → Depo ekle** bölümüne şu adresi girin:

```text
https://raw.githubusercontent.com/Yek5353/cloudstream-extensions/main/repo.json
```

Repo manifesti `builds` dalındaki `plugins.json` dosyasını kullanır. İlk başarılı yayın tamamlandıktan sonra eklenti listede görünür.

## Eklenti geliştirme

1. `ExampleProvider` klasörünü kopyalayıp yeni eklenti için yeniden adlandırın.
2. `ExamplePlugin.kt` içinde eklentiyi kaydedin. `ExampleProvider.kt` içinde sağlayıcı adını, site adresini, dili ve arama davranışını düzenleyin.
3. Modülün `build.gradle.kts` dosyasında açıklama, yazar, tür, dil ve sürümü ayarlayın.
4. JDK 17, Android SDK ve Gradle kurulu bilgisayarda proje kökünde `gradle make makePluginsJson` komutunu çalıştırabilirsiniz.
5. Değişiklikleri `main` dalına gönderdiğinizde GitHub Actions eklentileri derler ve `.cs3` paketleriyle `plugins.json` dosyasını otomatik olarak `builds` dalına yayımlar.

Actions derleme çıktısını ayrıca **Artifacts** bölümünde de saklar. Yayın için GitHub Actions’taki `publish` işi yalnızca gerekli `contents: write` iznini alır; diğer iş derleme sırasında sadece okuma izni kullanır.

## Kaynaklar

- [CloudStream eklenti şablonu kılavuzu](https://recloudstream.github.io/csdocs/devs/using-plugin-template/)
- [JSON repo manifesti kılavuzu](https://recloudstream.github.io/csdocs/devs/create-your-own-json-repository/)
- [Resmî TestPlugins şablonu](https://github.com/recloudstream/TestPlugins)
