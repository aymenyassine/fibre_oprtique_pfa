package com.fibre.optique.network.service;

import com.fibre.optique.network.dto.*;
import com.fibre.optique.network.entity.*;
import com.fibre.optique.network.exception.NetworkResourceNotFoundException;
import com.fibre.optique.network.exception.NetworkValidationException;
import com.fibre.optique.network.repository.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NetworkService {

    /** WGS84 SRID — standard for GPS coordinates */
    private static final int SRID = 4326;

    /**
     * Maximum distance in metres between the requested point and a BoiteClient
     * for the address to be considered eligible.
     */
    static final double MAX_ELIGIBILITY_DISTANCE_METRES = 500.0;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), SRID);

    private final DatacenterRepository    datacenterRepository;
    private final RepartiteurRepository   repartiteurRepository;
    private final SplitterRepository      splitterRepository;
    private final BoiteClientRepository   boiteClientRepository;
    private final EquipementRepository    equipementRepository;
    private final CheminFibreRepository   cheminFibreRepository;

    public NetworkService(DatacenterRepository datacenterRepository,
                          RepartiteurRepository repartiteurRepository,
                          SplitterRepository splitterRepository,
                          BoiteClientRepository boiteClientRepository,
                          EquipementRepository equipementRepository,
                          CheminFibreRepository cheminFibreRepository) {
        this.datacenterRepository  = datacenterRepository;
        this.repartiteurRepository = repartiteurRepository;
        this.splitterRepository    = splitterRepository;
        this.boiteClientRepository = boiteClientRepository;
        this.equipementRepository  = equipementRepository;
        this.cheminFibreRepository = cheminFibreRepository;
    }

    // =========================================================================
    // DATACENTER
    // =========================================================================

    public List<DatacenterDto> getAllDatacenters() {
        return datacenterRepository.findAll().stream()
                .map(DatacenterDto::fromEntity).toList();
    }

    public DatacenterDto getDatacenterById(Long id) {
        return datacenterRepository.findById(id)
                .map(DatacenterDto::fromEntity)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Datacenter", id));
    }

    @Transactional
    public DatacenterDto createDatacenter(DatacenterRequest request) {
        if (datacenterRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new NetworkValidationException(
                    "Un datacenter avec ce nom existe déjà : " + request.getNom());
        }
        Datacenter dc = Datacenter.builder()
                .nom(request.getNom())
                .capacite(request.getCapacite())
                .coordinate(buildPoint(request.getLongitude(), request.getLatitude()))
                .build();
        return DatacenterDto.fromEntity(datacenterRepository.save(dc));
    }

    @Transactional
    public DatacenterDto updateDatacenter(Long id, DatacenterRequest request) {
        Datacenter dc = datacenterRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Datacenter", id));
        dc.setNom(request.getNom());
        dc.setCapacite(request.getCapacite());
        dc.setCoordinate(buildPoint(request.getLongitude(), request.getLatitude()));
        return DatacenterDto.fromEntity(datacenterRepository.save(dc));
    }

    @Transactional
    public void deleteDatacenter(Long id) {
        if (!datacenterRepository.existsById(id)) {
            throw new NetworkResourceNotFoundException("Datacenter", id);
        }
        datacenterRepository.deleteById(id);
    }

    // =========================================================================
    // REPARTITEUR
    // =========================================================================

    public List<RepartiteurDto> getAllRepartiteurs() {
        return repartiteurRepository.findAll().stream()
                .map(RepartiteurDto::fromEntity).toList();
    }

    public RepartiteurDto getRepartiteurById(Long id) {
        return repartiteurRepository.findById(id)
                .map(RepartiteurDto::fromEntity)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Répartiteur", id));
    }

    @Transactional
    public RepartiteurDto createRepartiteur(RepartiteurRequest request) {
        Datacenter dc = datacenterRepository.findById(request.getDatacenterId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Datacenter", request.getDatacenterId()));

        if (repartiteurRepository.existsByNomIgnoreCase(request.getNom())) {
            throw new NetworkValidationException(
                    "Un répartiteur avec ce nom existe déjà : " + request.getNom());
        }

        Repartiteur rep = Repartiteur.builder()
                .nom(request.getNom())
                .nbPorts(request.getNbPorts())
                .coordinate(buildPoint(request.getLongitude(), request.getLatitude()))
                .datacenter(dc)
                .build();
        return RepartiteurDto.fromEntity(repartiteurRepository.save(rep));
    }

    @Transactional
    public RepartiteurDto updateRepartiteur(Long id, RepartiteurRequest request) {
        Repartiteur rep = repartiteurRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Répartiteur", id));

        Datacenter dc = datacenterRepository.findById(request.getDatacenterId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Datacenter", request.getDatacenterId()));

        rep.setNom(request.getNom());
        rep.setNbPorts(request.getNbPorts());
        rep.setCoordinate(buildPoint(request.getLongitude(), request.getLatitude()));
        rep.setDatacenter(dc);
        return RepartiteurDto.fromEntity(repartiteurRepository.save(rep));
    }

    @Transactional
    public void deleteRepartiteur(Long id) {
        if (!repartiteurRepository.existsById(id)) {
            throw new NetworkResourceNotFoundException("Répartiteur", id);
        }
        repartiteurRepository.deleteById(id);
    }

    // =========================================================================
    // SPLITTER
    // =========================================================================

    public List<SplitterDto> getAllSplitters() {
        return splitterRepository.findAll().stream()
                .map(SplitterDto::fromEntity).toList();
    }

    public SplitterDto getSplitterById(Long id) {
        return splitterRepository.findById(id)
                .map(SplitterDto::fromEntity)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Splitter", id));
    }

    @Transactional
    public SplitterDto createSplitter(SplitterRequest request) {
        Repartiteur rep = repartiteurRepository.findById(request.getRepartiteurId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Répartiteur", request.getRepartiteurId()));

        Splitter splitter = Splitter.builder()
                .ratio(request.getRatio())
                .nbSortie(request.getNbSortie())
                .repartiteur(rep)
                .build();
        return SplitterDto.fromEntity(splitterRepository.save(splitter));
    }

    @Transactional
    public SplitterDto updateSplitter(Long id, SplitterRequest request) {
        Splitter splitter = splitterRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Splitter", id));

        Repartiteur rep = repartiteurRepository.findById(request.getRepartiteurId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Répartiteur", request.getRepartiteurId()));

        splitter.setRatio(request.getRatio());
        splitter.setNbSortie(request.getNbSortie());
        splitter.setRepartiteur(rep);
        return SplitterDto.fromEntity(splitterRepository.save(splitter));
    }

    @Transactional
    public void deleteSplitter(Long id) {
        if (!splitterRepository.existsById(id)) {
            throw new NetworkResourceNotFoundException("Splitter", id);
        }
        splitterRepository.deleteById(id);
    }

    // =========================================================================
    // BOITE CLIENT
    // =========================================================================

    public List<BoiteClientDto> getAllBoitesClient() {
        return boiteClientRepository.findAll().stream()
                .map(BoiteClientDto::fromEntity).toList();
    }

    public BoiteClientDto getBoiteClientById(Long id) {
        return boiteClientRepository.findById(id)
                .map(BoiteClientDto::fromEntity)
                .orElseThrow(() -> new NetworkResourceNotFoundException("BoiteClient", id));
    }

    @Transactional
    public BoiteClientDto createBoiteClient(BoiteClientRequest request) {
        Splitter splitter = splitterRepository.findById(request.getSplitterId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Splitter", request.getSplitterId()));

        if (boiteClientRepository.existsByNom(request.getNom())) {
            throw new NetworkValidationException(
                    "Une boîte client avec ce nom existe déjà : " + request.getNom());
        }

        BoiteClient bc = BoiteClient.builder()
                .nom(request.getNom())
                .nbPorts(request.getNbPorts())
                .portsUtilises(request.getPortsUtilises() != null ? request.getPortsUtilises() : 0)
                .coordinate(buildPoint(request.getLongitude(), request.getLatitude()))
                .splitter(splitter)
                .build();
        return BoiteClientDto.fromEntity(boiteClientRepository.save(bc));
    }

    @Transactional
    public BoiteClientDto updateBoiteClient(Long id, BoiteClientRequest request) {
        BoiteClient bc = boiteClientRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("BoiteClient", id));

        Splitter splitter = splitterRepository.findById(request.getSplitterId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Splitter", request.getSplitterId()));

        if (request.getPortsUtilises() != null && request.getPortsUtilises() > request.getNbPorts()) {
            throw new NetworkValidationException(
                    "Les ports utilisés (" + request.getPortsUtilises() +
                    ") ne peuvent pas dépasser le nombre total de ports (" + request.getNbPorts() + ").");
        }

        bc.setNom(request.getNom());
        bc.setNbPorts(request.getNbPorts());
        bc.setPortsUtilises(request.getPortsUtilises() != null ? request.getPortsUtilises() : bc.getPortsUtilises());
        bc.setCoordinate(buildPoint(request.getLongitude(), request.getLatitude()));
        bc.setSplitter(splitter);
        return BoiteClientDto.fromEntity(boiteClientRepository.save(bc));
    }

    @Transactional
    public BoiteClientDto incrementPortsUtilises(Long id) {
        BoiteClient bc = boiteClientRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("BoiteClient", id));

        if (bc.getPortsUtilises() >= bc.getNbPorts()) {
            throw new NetworkValidationException(
                    "La boîte client '" + bc.getNom() + "' est saturée — plus de ports disponibles.");
        }
        bc.setPortsUtilises(bc.getPortsUtilises() + 1);
        return BoiteClientDto.fromEntity(boiteClientRepository.save(bc));
    }

    @Transactional
    public void deleteBoiteClient(Long id) {
        if (!boiteClientRepository.existsById(id)) {
            throw new NetworkResourceNotFoundException("BoiteClient", id);
        }
        boiteClientRepository.deleteById(id);
    }

    // =========================================================================
    // EQUIPEMENT
    // =========================================================================

    public List<EquipementDto> getAllEquipements() {
        return equipementRepository.findAll().stream()
                .map(EquipementDto::fromEntity).toList();
    }

    public EquipementDto getEquipementById(Long id) {
        return equipementRepository.findById(id)
                .map(EquipementDto::fromEntity)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Équipement", id));
    }

    @Transactional
    public EquipementDto createEquipement(EquipementRequest request) {
        Repartiteur rep = repartiteurRepository.findById(request.getRepartiteurId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Répartiteur", request.getRepartiteurId()));

        Equipement eq = Equipement.builder()
                .nom(request.getNom())
                .modele(request.getModele())
                .numSerie(request.getNumSerie())
                .ip(request.getIp())
                .status(request.getStatus())
                .type(request.getType())
                .repartiteur(rep)
                .build();
        return EquipementDto.fromEntity(equipementRepository.save(eq));
    }

    @Transactional
    public EquipementDto updateEquipement(Long id, EquipementRequest request) {
        Equipement eq = equipementRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("Équipement", id));

        Repartiteur rep = repartiteurRepository.findById(request.getRepartiteurId())
                .orElseThrow(() -> new NetworkResourceNotFoundException(
                        "Répartiteur", request.getRepartiteurId()));

        eq.setNom(request.getNom());
        eq.setModele(request.getModele());
        eq.setNumSerie(request.getNumSerie());
        eq.setIp(request.getIp());
        eq.setStatus(request.getStatus());
        eq.setType(request.getType());
        eq.setRepartiteur(rep);
        return EquipementDto.fromEntity(equipementRepository.save(eq));
    }

    @Transactional
    public void deleteEquipement(Long id) {
        if (!equipementRepository.existsById(id)) {
            throw new NetworkResourceNotFoundException("Équipement", id);
        }
        equipementRepository.deleteById(id);
    }

    // =========================================================================
    // CHEMIN FIBRE
    // =========================================================================

    public List<CheminFibreDto> getAllCheminsFibre() {
        return cheminFibreRepository.findAll().stream()
                .map(CheminFibreDto::fromEntity).toList();
    }

    public CheminFibreDto getCheminFibreById(Long id) {
        return cheminFibreRepository.findById(id)
                .map(CheminFibreDto::fromEntity)
                .orElseThrow(() -> new NetworkResourceNotFoundException("CheminFibre", id));
    }

    @Transactional
    public CheminFibreDto createCheminFibre(CheminFibreRequest request) {
        CheminFibre cf = CheminFibre.builder()
                .sourceNodeId(request.getSourceNodeId())
                .destNodeId(request.getDestNodeId())
                .longueur(request.getLongueur())
                .typeFibre(request.getTypeFibre())
                .statut(request.getStatut())
                .build();
        return CheminFibreDto.fromEntity(cheminFibreRepository.save(cf));
    }

    @Transactional
    public CheminFibreDto updateCheminFibre(Long id, CheminFibreRequest request) {
        CheminFibre cf = cheminFibreRepository.findById(id)
                .orElseThrow(() -> new NetworkResourceNotFoundException("CheminFibre", id));

        cf.setSourceNodeId(request.getSourceNodeId());
        cf.setDestNodeId(request.getDestNodeId());
        cf.setLongueur(request.getLongueur());
        cf.setTypeFibre(request.getTypeFibre());
        cf.setStatut(request.getStatut());
        return CheminFibreDto.fromEntity(cheminFibreRepository.save(cf));
    }

    @Transactional
    public void deleteCheminFibre(Long id) {
        if (!cheminFibreRepository.existsById(id)) {
            throw new NetworkResourceNotFoundException("CheminFibre", id);
        }
        cheminFibreRepository.deleteById(id);
    }

    // =========================================================================
    // ELIGIBILITY — real geo-distance via ST_Distance (MySQL spatial)
    // =========================================================================

    /**
     * Checks eligibility by plain text address.
     * If no GPS coordinates are available, returns eligible=false with a message
     * asking the caller to provide coordinates for a precise check.
     * Use {@link #checkEligibility(double, double)} for a real geospatial check.
     */
    public EligibilityResponse checkEligibility(String adresse) {
        // Without coordinates we cannot do a geospatial check — return ineligible
        return EligibilityResponse.builder()
                .eligible(false)
                .message("Vérification d'éligibilité par adresse texte non supportée. " +
                         "Veuillez fournir les coordonnées GPS (latitude/longitude) pour l'adresse : " + adresse)
                .build();
    }

    /**
     * Checks whether a given GPS position is eligible for fiber connection.
     * Uses ST_Distance via a native query in {@link BoiteClientRepository}
     * to find the nearest available BoiteClient within
     * {@value MAX_ELIGIBILITY_DISTANCE_METRES} metres.
     *
     * @param longitude WGS84 longitude of the address to check
     * @param latitude  WGS84 latitude of the address to check
     * @return eligibility result with distance and nearest box name
     */
    public EligibilityResponse checkEligibility(double longitude, double latitude) {
        validateCoordinates(longitude, latitude);

        // Format: POINT(longitude latitude) — WKT standard, US locale ensures dot decimal separator
        String pointWkt = String.format(java.util.Locale.US, "POINT(%f %f)", longitude, latitude);

        return boiteClientRepository.findNearestAvailableBox(pointWkt)
                .map(bc -> {
                    double distM = haversineMetres(
                            latitude, longitude,
                            bc.getCoordinate().getY(), bc.getCoordinate().getX());

                    if (distM > MAX_ELIGIBILITY_DISTANCE_METRES) {
                        return EligibilityResponse.builder()
                                .eligible(false)
                                .distanceMetres(distM)
                                .message("La boîte client la plus proche est à %.0f m — hors zone éligible (max %d m)."
                                        .formatted(distM, (int) MAX_ELIGIBILITY_DISTANCE_METRES))
                                .build();
                    }

                    return EligibilityResponse.builder()
                            .eligible(true)
                            .technologieDisponible("FTTH")
                            .boiteClientNom(bc.getNom())
                            .distanceMetres(distM)
                            .message("Adresse éligible FTTH. Boîte disponible : %s (%.0f m)."
                                    .formatted(bc.getNom(), distM))
                            .build();
                })
                .orElseGet(() -> EligibilityResponse.builder()
                        .eligible(false)
                        .message("Aucune boîte client disponible trouvée dans la base.")
                        .build());
    }

    // =========================================================================
    // NETWORK STATUS
    // =========================================================================

    public NetworkStatusResponse getNetworkStatus() {
        List<CheminFibre> incidents = cheminFibreRepository.findByStatut("INCIDENT");
        long boitesAvecPortsLibres = boiteClientRepository.findAvailableBoxes().size();

        return NetworkStatusResponse.builder()
                .totalDatacenters(datacenterRepository.count())
                .totalRepartiteurs(repartiteurRepository.count())
                .totalSplitters(splitterRepository.count())
                .totalEquipements(equipementRepository.count())
                .totalBoitesClient(boiteClientRepository.count())
                .boitesAvecPortsLibres(boitesAvecPortsLibres)
                .totalCheminsFibre(cheminFibreRepository.count())
                .cheminsEnIncident(incidents.size())
                .activeIncidents(incidents.stream().map(CheminFibreDto::fromEntity).toList())
                .build();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /** Builds a JTS Point with WGS84 SRID from longitude/latitude. */
    private Point buildPoint(double longitude, double latitude) {
        Point p = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        p.setSRID(SRID);
        return p;
    }

    private void validateCoordinates(double lon, double lat) {
        if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
            throw new NetworkValidationException(
                    "Coordonnées GPS invalides — longitude: " + lon + ", latitude: " + lat);
        }
    }

    /**
     * Haversine formula — computes great-circle distance in metres between two GPS points.
     * Used as a fallback / double-check after the DB spatial query.
     */
    static double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000.0; // Earth radius in metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
